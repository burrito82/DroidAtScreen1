package com.ribomation.droidAtScreen.cmd;

import com.ribomation.droidAtScreen.Application;
import com.ribomation.droidAtScreen.gui.DeviceFrame;

import javax.swing.*;

/**
 * Created by Somebody on 30.11.2015.
 */
public class ScreenshotAllCommand extends CommandWithTarget<DeviceFrame> {

    public ScreenshotAllCommand(DeviceFrame target) {
        super(target);
        updateButton(target);
        setIcon("howto");
    }

    @Override
    protected void updateButton(DeviceFrame target) {

    }

    @Override
    protected void doExecute(Application app, DeviceFrame target) {
        JOptionPane.showMessageDialog(app.getAppFrame(), "Test");
    }
}
