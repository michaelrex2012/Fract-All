package org.rendering;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import org.math.ComplexNumber;
import org.math.MandelbrotSet;
import java.awt.event.*;

public class ScreenDrawer extends JPanel {
    private final int width;
    private final int height;
    private final int maxIterations;
    private BufferedImage bufferedImage;
    private BufferedImage lowResImage;
    private Timer zoomTimer;
    private int interations = 500;

    // Define the boundaries of the complex plane
    private double minReal = -2;
    private double maxReal = 1;
    private double minImaginary = -1.5;
    private double maxImaginary = 1.5;
    private double zoomFactor = 1.2; // Zoom factor per scroll
    private boolean isZooming = false; // To track zooming state

    public ScreenDrawer(int width, int height, int maxIterations) {
        this.width = width;
        this.height = height;
        this.maxIterations = maxIterations;
        this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        this.lowResImage = new BufferedImage(width / 4, height / 4, BufferedImage.TYPE_INT_RGB); // Low resolution image

        setPreferredSize(new Dimension(width, height)); // No extra space for progress bar

        // Add mouse wheel listener for zooming
        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int scrollDirection = e.getWheelRotation(); // Positive for zoom out, negative for zoom in
                zoom(e.getX(), e.getY(), scrollDirection);

                // Stop previous timer if still running
                if (zoomTimer != null && zoomTimer.isRunning()) {
                    zoomTimer.stop();
                }

                // Delay rendering full resolution until zooming stops
                zoomTimer = new Timer(500, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        isZooming = false;
                        renderMandelbrotSet(); // Render full quality after zooming stops
                        zoomTimer.stop();
                    }
                });
                zoomTimer.setRepeats(false); // Ensure the timer only runs once
                zoomTimer.start();

                // Start rendering low quality during zooming
                isZooming = true;
                renderLowResMandelbrot();
            }
        });

        renderMandelbrotSet();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (isZooming) {
            // Draw low resolution image while zooming
            g2d.drawImage(lowResImage, 0, 0, width, height, null);
        } else {
            // Draw full resolution image after zooming
            g2d.drawImage(bufferedImage, 0, 0, null);
        }
    }

    private void zoom(int mouseX, int mouseY, int scrollDirection) {
        // Map mouse click to complex plane coordinates
        ComplexNumber center = mapToComplex(mouseX, mouseY);

        // Adjust zoom factor based on scroll direction
        double factor = (scrollDirection < 0) ? 1 / zoomFactor : zoomFactor;

        double realRange = (maxReal - minReal) * factor;
        double imaginaryRange = (maxImaginary - minImaginary) * factor;

        // Adjust the real and imaginary boundaries around the zoom center
        minReal = center.getReal() - realRange / 2;
        maxReal = center.getReal() + realRange / 2;
        minImaginary = center.getImaginary() - imaginaryRange / 2;
        maxImaginary = center.getImaginary() + imaginaryRange / 2;

        repaint();
    }

    // Render Mandelbrot set at full resolution
    private void renderMandelbrotSet() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                ForkJoinPool pool = new ForkJoinPool();
                MandelbrotTask task = new MandelbrotTask(0, width, 0, height, bufferedImage);
                pool.invoke(task);
                pool.shutdown();
                return null;
            }

            @Override
            protected void done() {
                repaint(); // Repaint the final image after rendering is complete
            }
        };

        worker.execute(); // Start rendering in the background
    }

    // Render Mandelbrot set at low resolution
    private void renderLowResMandelbrot() {
        ForkJoinPool pool = new ForkJoinPool();
        MandelbrotTask task = new MandelbrotTask(0, width / 4, 0, height / 4, lowResImage);
        pool.invoke(task);
        pool.shutdown();
        repaint(); // Repaint with the low resolution image
    }

    // Recursive task for Mandelbrot set computation
    private class MandelbrotTask extends RecursiveTask<Void> {
        private static final int THRESHOLD = 100;
        private int startX, endX, startY, endY;
        private BufferedImage image;

        public MandelbrotTask(int startX, int endX, int startY, int endY, BufferedImage image) {
            this.startX = startX;
            this.endX = endX;
            this.startY = startY;
            this.endY = endY;
            this.image = image;
        }

        @Override
        protected Void compute() {
            if ((endX - startX) * (endY - startY) <= THRESHOLD) {
                computeDirectly();
                return null;
            }

            int midX = (startX + endX) / 2;
            int midY = (startY + endY) / 2;

            invokeAll(
                    new MandelbrotTask(startX, midX, startY, midY, image),
                    new MandelbrotTask(midX, endX, startY, midY, image),
                    new MandelbrotTask(startX, midX, midY, endY, image),
                    new MandelbrotTask(midX, endX, midY, endY, image)
            );
            return null;
        }

        private void computeDirectly() {
            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {
                    ComplexNumber complexNumber = mapToComplex(x, y, image.getWidth(), image.getHeight());
                    int iterations = MandelbrotSet.getIterations(complexNumber, maxIterations);
                    image.setRGB(x, y, getColor(iterations).getRGB());
                }
            }
            Toolkit.getDefaultToolkit().sync();
        }
    }

    // Map screen coordinates to complex plane
    private ComplexNumber mapToComplex(int x, int y, int imageWidth, int imageHeight) {
        double real = minReal + x * (maxReal - minReal) / (imageWidth - 1);
        double imaginary = minImaginary + y * (maxImaginary - minImaginary) / (imageHeight - 1);
        return new ComplexNumber(real, imaginary);
    }

    private ComplexNumber mapToComplex(int x, int y) {
        return mapToComplex(x, y, width, height);
    }

    // Get the color based on the number of iterations
    private Color getColor(int iterations) {
        if (iterations == maxIterations) {
            return Color.BLACK;
        }
        float hue = iterations / 256.0f;
        float saturation = 1.0f;
        float brightness = iterations / (iterations + 8.0f);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static void bAndWhiteDraw() {
        int width = 800;
        double aspectRatio = 1.0;
        int height = (int) (width / aspectRatio);

        private void openSettingsDialog(JFrame parentFrame) {
        // Create a modal dialog
        JDialog settingsDialog = new JDialog(parentFrame, "Set Iterations", true);
        settingsDialog.setSize(300, 150);
        settingsDialog.setLayout(new FlowLayout());
        settingsDialog.setLocationRelativeTo(parentFrame);

        // Label for integer input
        JLabel label = new JLabel("Enter a new value for myVariable:");

        // Integer input field
        JTextField integerField = new JTextField(10);

        // OK button to confirm the change
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Get the entered value and update myVariable
                    int newValue = Integer.parseInt(integerField.getText());
                    myVariable = newValue;
                    System.out.println("Interations have been set to: " + );
                    settingsDialog.dispose(); // Close the dialog
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(settingsDialog,
                            "Please enter a valid integer.",
                            "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ScreenDrawer drawer = new ScreenDrawer(width, height, 500);
        JFrame frame = new JFrame("Fract-All");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(drawer, BorderLayout.CENTER); // No progress bar
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        drawer.renderMandelbrotSet(); // Start rendering
    }
}
