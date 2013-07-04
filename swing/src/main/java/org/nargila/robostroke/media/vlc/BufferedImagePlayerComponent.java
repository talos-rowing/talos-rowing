package org.nargila.robostroke.media.vlc;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class BufferedImagePlayerComponent extends BufferedImageMediaPlayer {

    private final JPanel canvas;
    private final Font font = new Font("Sansserif", Font.BOLD, 36);

    @SuppressWarnings("serial")
    public BufferedImagePlayerComponent() {
        canvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                int w = getWidth();
                int h = getHeight();

                g.setColor(Color.black);

                g.fillRect(0, 0, w, h);

                BufferedImage image = lockImage();

                try {
                    float width = w;
                    float height = h;
                    float xyScaleCanvas = width / height;
                    float xyScaleImage = (float)image.getWidth() / image.getHeight();
                    float renderWidth;
                    float renderHeight;

                    if (xyScaleImage <= xyScaleCanvas) { // landscape image - height shorter
                        renderWidth = Math.min(height * xyScaleImage, width);
                        renderHeight = renderWidth / xyScaleImage;
                    } else { // portrait image - width shorter
                        renderHeight = Math.min(width / xyScaleImage, height);
                        renderWidth = renderHeight * xyScaleImage;
                    }

                    float x = (width - renderWidth) / 2;
                    float y = (height - renderHeight) / 2;

                    Graphics2D g2 = (Graphics2D)g;

                    g2.drawImage(image, new AffineTransform(renderWidth / image.getWidth(), 0f, 0f, renderHeight / image.getHeight(), x, y), null);
                    // g2.setColor(Color.white);
                    // g2.setFont(font);
                    // g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    // RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    // g2.drawString("lightweight overlay", 100, 200);
                } finally {
                    releaseImage(image);
                }
            }
        };

        canvas.setBackground(Color.black);
        canvas.setOpaque(true);

    }

    @Override
    protected void onImageChanged(BufferedImage image, long timestamp) {
        canvas.repaint();
    }

    public JPanel getCanvas() {
        return canvas;
    }

    public void start(String mrl) {
        getMediaPlayer().playMedia(mrl);
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Specify an mrl");
            System.exit(1);
        }

        final BufferedImagePlayerComponent player = new BufferedImagePlayerComponent();

        final String mrl = args[0];

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("BufferedImagePlayerComponent Test");

                frame.setContentPane(player.getCanvas());

                frame.setLocation(100, 100);
                frame.setSize(1050, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                player.start(mrl);
            }
        });
    }
}
