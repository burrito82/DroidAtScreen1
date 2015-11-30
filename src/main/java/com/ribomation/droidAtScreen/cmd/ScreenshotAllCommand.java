package com.ribomation.droidAtScreen.cmd;

import com.ribomation.droidAtScreen.Application;
import com.ribomation.droidAtScreen.dev.ScreenImage;
import com.ribomation.droidAtScreen.gui.DeviceFrame;
import com.sun.glass.ui.Screen;

import javax.swing.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    @Override
    protected void doExecute(Application app) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("Devices:\n");
        List<DeviceFrame> listDevices = getApplication().getDevices();
        for (DeviceFrame deviceFrame : listDevices) {
            strBuilder.append(deviceFrame.getDevice().getName()).append(" - ");
            ScreenImage image = deviceFrame.getLastScreenshot();
        }
        JOptionPane.showMessageDialog(app.getAppFrame(), strBuilder.toString());
    }
}
