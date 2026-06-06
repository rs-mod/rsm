package com.ricedotwho.rsm.utils.render.render2d;

import lombok.Getter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class GIF {
    private final List<Image> frames;
    private final int[] delays;
    private final long duration;
    private final int[] cumuliDelay;

    private final List<BufferedImage> compositedFrames = new ArrayList<>();
    private String prefix;

    public GIF(List<Image> frames, List<Integer> delayList) {
        this.frames = frames;
        this.delays = delayList.stream().mapToInt(i -> i).toArray();
        this.cumuliDelay = new int[delays.length];

        int sum = 0;
        for (int i = 0; i < delays.length; i++) {
            sum += delays[i];
            cumuliDelay[i] = sum;
        }

        this.duration = sum;
    }

    public Image getCurrent() {
        long now = System.currentTimeMillis();
        int time = (int) (now % this.duration);
        return getCurrent(time);
    }

    public Image getCurrent(long now) {
        int index = Arrays.binarySearch(cumuliDelay, (int) now);
        if (index < 0) {
            index = -index - 1;
        }
        return frames.get(index);
    }

    public GIF(String prefix, InputStream input) throws IOException {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        ImageInputStream stream = ImageIO.createImageInputStream(input);
        reader.setInput(stream, false);
        int frameCount = reader.getNumImages(true);
        BufferedImage first = reader.read(0);
        int width = first.getWidth();
        int height = first.getHeight();

        List<Integer> delays = new ArrayList<>();

        BufferedImage master = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = master.createGraphics();

        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = reader.read(i);
            IIOMetadata metadata = reader.getImageMetadata(i);
            int delay = extractDelay(metadata);
            delays.add(delay);

            // gifs are kinda cool they can do like only store changes in each frame, but it does make this pretty annoying, and slow, and I can't stop it from blocking...
            DisposalType disposal = getDisposalMethod(metadata);
            if (disposal == DisposalType.BACKGROUND) {
                graphics.clearRect(0, 0, width, height);
            }

            int x = 0;
            int y = 0;
            String metaFormat = metadata.getNativeMetadataFormatName();
            Node root = metadata.getAsTree(metaFormat);
            Node gceNode = findNode(root, "ImageDescriptor");
            if (gceNode != null) {
                x = Integer.parseInt(gceNode.getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                y = Integer.parseInt(gceNode.getAttributes().getNamedItem("imageTopPosition").getNodeValue());
            }
            graphics.drawImage(frame, x, y, null);
            BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = copy.createGraphics();
            g2.drawImage(master, 0, 0, null);
            g2.dispose();

            compositedFrames.add(copy);
        }
        graphics.dispose();

        this.prefix = prefix + "_gif_frame_";
        this.frames = new ArrayList<>();
        this.delays = delays.stream().mapToInt(x -> x).toArray();

        // is this even useful
        this.cumuliDelay = new int[this.delays.length];
        int sum = 0;
        for (int i = 0; i < this.delays.length; i++) {
            sum += this.delays[i];
            cumuliDelay[i] = sum;
        }

        this.duration = sum;
    }

    public void create() {
        for (int i = 0; i < compositedFrames.size(); i++) {
            frames.add(NVGUtils.createImage(prefix + i, compositedFrames.get(i)));
        }
    }

    private static int extractDelay(IIOMetadata metadata) {
        String metaFormat = metadata.getNativeMetadataFormatName();
        Node root = metadata.getAsTree(metaFormat);
        return extractDelayFromNode(root);
    }

    private static int extractDelayFromNode(Node root) {
        NodeList nodes = root.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if ("GraphicControlExtension".equals(node.getNodeName())) {
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null) {
                    Node delayNode = attrs.getNamedItem("delayTime");
                    if (delayNode != null) {
                        try {
                            int delay = Integer.parseInt(delayNode.getNodeValue());
                            return delay * 10;
                        } catch (NumberFormatException e) {
                            return 100;
                        }
                    }
                }
            }
            int childDelay = extractDelayFromNode(node);
            if (childDelay != -1) return childDelay;
        }
        return 100;
    }

    public static DisposalType getDisposalMethod(IIOMetadata metadata) {
        String metaFormat = metadata.getNativeMetadataFormatName();
        Node root = metadata.getAsTree(metaFormat);

        Node gceNode = findNode(root, "GraphicControlExtension");
        if (gceNode != null) {
            String disposal = gceNode.getAttributes().getNamedItem("disposalMethod").getNodeValue();
            return switch (disposal) {
                case "restoreToBackgroundColor" -> DisposalType.BACKGROUND; // clear to background
                case "restoreToPrevious" -> DisposalType.PREVIOUS; // restore previous frame
                default -> DisposalType.NONE;
            };
        }
        return DisposalType.NONE;
    }

    private static Node findNode(Node node, String name) {
        if (node.getNodeName().equals(name)) return node;
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            Node found = findNode(child, name);
            if (found != null) return found;
        }
        return null;
    }

    public void delete() {
        for (Image image : this.frames) {
            image.delete();
        }
    }

    public enum DisposalType {
        NONE,
        BACKGROUND,
        PREVIOUS
    }
}
