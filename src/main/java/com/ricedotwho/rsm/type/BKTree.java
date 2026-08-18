package com.ricedotwho.rsm.type;

import com.ricedotwho.rsm.utils.MathUtils;
import lombok.Getter;
import lombok.val;

import java.util.*;

public class BKTree<T> {

    private static final int ALIAS_PENALTY = 25;

    @Getter
    public static class Result<T> {
        private final T value;
        private final int score;

        Result(T value, int score) {
            this.value = value;
            this.score = score;
        }
    }

    private static class BKTreeNode<T> {
        final T value;
        final String word;
        final boolean alias;
        final Map<Integer, BKTreeNode<T>> children = new HashMap<>();

        BKTreeNode(T value, String word, boolean alias) {
            this.value = value;
            this.word = word;
            this.alias = alias;
        }
    }

    private BKTreeNode<T> root;

    /** Adds a value indexed by its primary word. */
    public void add(T value, String word) {
        insert(value, word, false);
    }

    /** Adds a value indexed by its primary word, plus alias words scored with a penalty. */
    public void add(T value, String word, List<String> aliases) {
        insert(value, word, false);
        for (String alias : aliases) {
            insert(value, alias, true);
        }
    }

    public void add(T value, String word, String[] aliases) {
        insert(value, word, false);
        for (String alias : aliases) {
            insert(value, alias, true);
        }
    }

    /** Adds lower-weighted string pointing at an existing (or new) value. */
    public void addAlias(T value, String alias) {
        insert(value, alias, true);
    }

    private void insert(T value, String word, boolean alias) {
        BKTreeNode<T> node = new BKTreeNode<>(value, word, alias);
        if (root == null) {
            root = node;
            return;
        }
        BKTreeNode<T> current = root;
        int distance = levenshteinDistance(word, current.word);
        while (current.children.containsKey(distance)) {
            current = current.children.get(distance);
            distance = levenshteinDistance(word, current.word);
        }
        current.children.put(distance, node);
    }

    /**
     * Searches the tree, scoring every matched node (primary or alias) the same way as the
     * standalone {@code score} function, applying a penalty for alias matches. If a value has
     * multiple matching nodes (e.g. primary word + alias), only the best score is kept.
     */
    public List<Result<T>> search(String query, int threshold) {
        if (root == null) {
            return new ArrayList<>();
        }
        Map<T, Integer> bestScores = new HashMap<>();
        Deque<Map.Entry<BKTreeNode<T>, Integer>> candidates = new ArrayDeque<>();
        candidates.push(new AbstractMap.SimpleEntry<>(root, threshold));

        while (!candidates.isEmpty()) {
            Map.Entry<BKTreeNode<T>, Integer> current = candidates.pop();
            BKTreeNode<T> node = current.getKey();
            int maxDist = current.getValue();
            int distance = levenshteinDistance(query, node.word);

            if (distance <= threshold) {
                int nodeScore = score(node.word, query);
                if (node.alias) {
                    nodeScore -= ALIAS_PENALTY;
                }
                bestScores.merge(node.value, nodeScore, Math::max);
            }

            int minChildDist = distance - maxDist;
            int maxChildDist = distance + maxDist;
            for (Map.Entry<Integer, BKTreeNode<T>> entry : node.children.entrySet()) {
                int childDist = entry.getKey();
                if (childDist >= minChildDist && childDist <= maxChildDist) {
                    candidates.push(new AbstractMap.SimpleEntry<>(entry.getValue(), maxDist));
                }
            }
        }

        List<Result<T>> results = new ArrayList<>(bestScores.size());
        for (Map.Entry<T, Integer> entry : bestScores.entrySet()) {
            results.add(new Result<>(entry.getKey(), entry.getValue()));
        }
        results.sort(Comparator.comparingInt(Result<T>::getScore).reversed());
        return results;
    }

    private static int score(String candidate, String query) {
        candidate = candidate.toLowerCase(Locale.ROOT);
        query = query.toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return 0;
        if (candidate.equals(query)) return 1000;
        if (candidate.startsWith(query)) return 850;

        for (String word : candidate.split("[ _-]")) {
            if (word.startsWith(query)) return 800;
        }

        if (candidate.contains(query)) return 650;

        int qi = 0;
        for (int i = 0; i < candidate.length() && qi < query.length(); i++) {
            if (candidate.charAt(i) == query.charAt(qi)) qi++;
        }
        if (qi == query.length()) return 500;

        int dist = levenshteinDistance(candidate, query);
        return Math.max(0, 400 - dist * 25);
    }

    private static int levenshteinDistance(String s1, String s2) {
        if (s1.length() > s2.length()) {
            val temp = s1;
            s1 = s2;
            s2 = temp;
        }
        var previousRow = new int[s2.length() + 1];
        for (int i = 0; i <= s2.length(); i++) {
            previousRow[i] = i;
        }
        var currentRow = new int[s2.length() + 1];
        for (int i = 1; i <= s1.length(); i++) {
            currentRow[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                val cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                currentRow[j] = MathUtils.min(
                        previousRow[j] + 1,
                        currentRow[j - 1] + 1,
                        previousRow[j - 1] + cost
                );
            }
            val temp = currentRow;
            currentRow = previousRow;
            previousRow = temp;
        }
        return previousRow[s2.length()];
    }
}