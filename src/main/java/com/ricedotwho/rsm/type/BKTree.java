package com.ricedotwho.rsm.type;

import com.ricedotwho.rsm.utils.MathUtils;
import lombok.val;

import java.util.*;


public class BKTree {
    public static class BKTreeNode {
        String word;
        Map<Integer, BKTreeNode> children = new HashMap<>();

        BKTreeNode(String word) {
            this.word = word;
        }
    }

    private BKTreeNode root;


    public void add(String word) {
        if (root == null) {
            root = new BKTreeNode(word);
            return;
        }


        BKTreeNode current = root;

        int distance = levenshteinDistance(word, current.word);

        while (current.children.containsKey(distance)) {
            current = current.children.get(distance);
            distance = levenshteinDistance(word, current.word);
        }

        current.children.put(distance, new BKTreeNode(word));
    }

    public List<Map.Entry<String, Integer>> search(String query, int threshold) {
        if (root == null) {
            return new ArrayList<>();
        }

        List<Map.Entry<String, Integer>> results = new ArrayList<>();
        Deque<Map.Entry<BKTreeNode, Integer>> candidates = new ArrayDeque<>();
        candidates.push(new AbstractMap.SimpleEntry<>(root, threshold));

        while (!candidates.isEmpty()) {
            Map.Entry<BKTreeNode, Integer> current = candidates.pop();
            BKTreeNode node = current.getKey();
            int maxDist = current.getValue();

            int distance = levenshteinDistance(query, node.word);

            if (distance <= threshold) {
                results.add(new AbstractMap.SimpleEntry<>(node.word, distance));
            }

            int minChildDist = distance - maxDist;
            int maxChildDist = distance + maxDist;

            for (Map.Entry<Integer, BKTreeNode> entry : node.children.entrySet()) {
                int childDist = entry.getKey();
                if (childDist >= minChildDist && childDist <= maxChildDist) {
                    candidates.push(new AbstractMap.SimpleEntry<>(entry.getValue(), maxDist));
                }
            }
        }

        results.sort(Comparator.comparingInt(Map.Entry::getValue));
        return results;
    }

    private int levenshteinDistance(String s1, String s2) {
        if (s1.length() > s2.length()) {
            val temp = s1;
            s1 = s2;
            s2 = temp;
        }
        var previousRow = new int[s2.length() + 1];
        for (int i = 0; i <= s2.length(); i++) {
            previousRow[i] =  i;
        }
        var currentRow = new int[s2.length() + 1];

        for (int i = 1; i <= s1.length(); i++) {
            currentRow[0] = i;

            for (int j = 1; j <= s2.length(); j++) {
                val cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                currentRow[j] = MathUtils.min(
                        previousRow[j] + 1,
                        currentRow[j-1] + 1,
                        previousRow[j-1] + cost
                );
            }
            val temp = currentRow;
            currentRow = previousRow;
            previousRow = temp;
        }
        return previousRow[s2.length()];
    }
}