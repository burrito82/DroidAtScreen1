package com.ribomation.droidAtScreen.cmd;

import com.ribomation.droidAtScreen.Application;
import com.ribomation.droidAtScreen.Settings;
import com.ribomation.droidAtScreen.dev.ScreenImage;
import com.ribomation.droidAtScreen.gui.DeviceFrame;
import com.sun.glass.ui.Screen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Somebody on 30.11.2015.
 */
public class ScreenshotAllCommand extends Command {

    public ScreenshotAllCommand() {
        super();
        setLabel("Screenshot All");
        setIcon("howto");
        setMnemonic('X');
        setTooltip("Make one big screenshot including all devices.");

        AddAnnotation(new IAnnotationGenerator() {
            @Override
            public String generate() {
                return getIP();
            }
        });

        AddAnnotation(new IAnnotationGenerator() {
            @Override
            public String generate() {
                return "Hello World";
            }
        });
    }

    public String getIP(){
        String IP= null;
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return IP;
    }

    private JFileChooser createChooser(File dir, File file, String[] exts) {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(dir);
        chooser.setSelectedFile(file);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", exts));
        return chooser;
    }

    private File suggestFilename(Application app) {
        Settings cfg = app.getSettings();
        return new File(cfg.getImageDirectory(), String.format("%s-%d.%s", app.getInfo().getName().toLowerCase(), cfg.nextInt(), cfg.getImageFormat().toLowerCase()));
    }

    private boolean askOverwrite(Application app, File f) {
        return JOptionPane.showConfirmDialog(app.getAppFrame(), String.format("File '%s' already exists. Do you want to overwrite it?", f), "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private class ImageSaver implements Runnable {
        private Application app;
        private File file;
        private BufferedImage image;

        private ImageSaver(Application app, File file, BufferedImage image) {
            this.app = app;
            this.file = file;
            this.image = image;
        }

    @Override
        public void run() {
            try {
                ImageIO.write(image, extractFormat(app, file), file);
                app.getAppFrame().getStatusBar().message("Written %s", file.getName());
                getLog().info(String.format("Screenshot file: %s", file.getAbsolutePath()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(app.getAppFrame(), String.format("Failed to save '%s': %s", file, e.getMessage()), "Failure", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String extractExt(File f) {
        String n = f.getName();
        int dot = n.lastIndexOf('.');
        if (dot > 0)
            return n.substring(dot + 1);
        return n;
    }

    private String extractFormat(Application app, File f) {
        String[] formats = app.getSettings().getImageFormats();
        String ext = extractExt(f).toUpperCase();
        if (Arrays.asList(formats).contains(ext))
            return ext;
        throw new RuntimeException("Invalid extension: " + f);
    }

    private interface IAnnotationGenerator {
        public String generate();
    }

    public void AddAnnotation(IAnnotationGenerator annotationGenerator)
    {
        mLiAnnotations.add(annotationGenerator);
    }

    private void AnnotateScreenshot(BufferedImage image)
    {
        int w_ = 0;
        int h_ = 0;

        Font font = new Font("Tahoma", Font.PLAIN, 42);
        FontRenderContext frc = new FontRenderContext(null, true, true);

        Graphics2D g = image.createGraphics();
        for (IAnnotationGenerator gen : mLiAnnotations) {
            String textToWrite = gen.generate();

            Rectangle2D bounds = font.getStringBounds(textToWrite, frc);
            int w = (int) bounds.getWidth();
            int h = (int) bounds.getHeight();

            g.setColor(Color.BLACK);
            g.fillRect(0, h_, image.getWidth(), h_ + h + 2);
            g.setColor(Color.WHITE);
            g.fillRect(0, h_, image.getWidth(), h_ + h);
            g.setColor(Color.BLACK);
            g.setFont(font);
            g.drawString(textToWrite, (float) bounds.getX(), (float) (h_ - bounds.getY()));

            h_ += h + 1;
        }
        g.dispose();
    }

    ArrayList<IAnnotationGenerator> mLiAnnotations = new ArrayList<IAnnotationGenerator>();

    @Override
    protected void doExecute(Application app) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append(getIP()).append("Devices:\n");
        List<DeviceFrame> listDevices = getApplication().getDevices();
        for (DeviceFrame deviceFrame : listDevices) {
            strBuilder.append(deviceFrame.getDevice().getDevice().getSerialNumber());
            ScreenImage aImage = deviceFrame.getLastScreenshot();
            BufferedImage jImage = aImage.toBufferedImage();

            String textToWrite = getIP() + " | " + deviceFrame.getDevice().getDevice().getSerialNumber();

            AnnotateScreenshot(jImage);

            JFileChooser chooser = createChooser(app.getSettings().getImageDirectory(), suggestFilename(app), app.getSettings().getImageFormats());
            if (chooser.showSaveDialog(app.getAppFrame()) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (!file.exists() || askOverwrite(app, file)) {
                    SwingUtilities.invokeLater(new ImageSaver(app, file, jImage));
        }
            }
        }
        JOptionPane.showMessageDialog(app.getAppFrame(), strBuilder.toString());
    }
}
