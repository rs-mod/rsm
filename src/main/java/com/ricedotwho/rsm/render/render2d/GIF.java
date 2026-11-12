package com.ricedotwho.rsm.render.render2d;

import com.mojang.blaze3d.platform.NativeImage;
import lombok.Getter;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
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

    private final List<NativeImage> compositedFrames = new ArrayList<>();
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
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);

        int previousX = 0;
        int previousY = 0;
        int previousWidth = 0;
        int previousHeight = 0;
        DisposalType prev = DisposalType.NONE;

        for (int i = 0; i < frameCount; i++) {
            BufferedImage frame = reader.read(i);
            IIOMetadata metadata = reader.getImageMetadata(i);

            int delay = extractDelay(metadata);
            delays.add(delay);

            if (prev == DisposalType.BACKGROUND) {
                clearRect(image, previousX, previousY, previousWidth, previousHeight);
            }

            String meta = metadata.getNativeMetadataFormatName();
            Node root = metadata.getAsTree(meta);
            Node desc = findNode(root, "ImageDescriptor");

            int x = 0;
            int y = 0;

            if (desc != null) {
                x = Integer.parseInt(desc.getAttributes().getNamedItem("imageLeftPosition").getNodeValue());
                y = Integer.parseInt(desc.getAttributes().getNamedItem("imageTopPosition").getNodeValue());
            }

            for (int fy = 0; fy < frame.getHeight(); fy++) {
                for (int fx = 0; fx < frame.getWidth(); fx++) {
                    int px = frame.getRGB(fx, fy);

                    if ((px >>> 24) == 0) continue;

                    image.setPixel(x + fx, y + fy, px);
                }
            }

            NativeImage copy = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            copy.copyFrom(image);
            compositedFrames.add(copy);

            prev = getDisposalMethod(metadata);
            previousX = x;
            previousY = y;
            previousWidth = frame.getWidth();
            previousHeight = frame.getHeight();
        }

        image.close();

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

    private static void clearRect(NativeImage image, int x, int y, int width, int height) {
        int right = Math.min(x + width, image.getWidth());
        int bottom = Math.min(y + height, image.getHeight());

        for (int py = Math.max(0, y); py < bottom; py++) {
            for (int px = Math.max(0, x); px < right; px++) {
                image.setPixel(px, py, 0);
            }
        }
    }

    public void create() {
        for (int i = 0; i < compositedFrames.size(); i++) {
            frames.add(new Image(prefix + i, compositedFrames.get(i)));
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

    public void close() {
        for (Image image : this.frames) {
            image.close();
        }
    }

    public enum DisposalType {
        NONE,
        BACKGROUND,
        PREVIOUS
    }
}
