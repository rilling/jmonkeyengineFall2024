/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.app;

import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeSystem;
import com.jme3.util.res.Resources;

import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Graphics;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kirill Vainer
 */
public class AppletHarness extends Applet {

    protected static final HashMap<LegacyApplication, Applet> appToApplet
            = new HashMap<LegacyApplication, Applet>();

    private static final Logger LOGGER = Logger.getLogger(AppletHarness.class.getName());

    protected JmeCanvasContext context;
    protected Canvas canvas;
    protected LegacyApplication app;

    protected String appClass;
    protected URL appCfg = null;
    protected URL assetCfg = null;

    public static Applet getApplet(Application app){
        return appToApplet.get(app);
    }

    @SuppressWarnings("unchecked")
    private void createCanvas(){
        AppSettings settings = new AppSettings(true);

        // load app cfg
        if (appCfg != null){
            InputStream in = null;
            try {
                in = appCfg.openStream();
                settings.load(in);
            } catch (IOException ex){
                // Called before application has been created ....
                // Display error message through AWT
                JOptionPane.showMessageDialog(this, "An error has occurred while "
                                + "loading applet configuration: "
                                + ex.getMessage(),
                        "jME3 Applet",
                        JOptionPane.ERROR_MESSAGE);
                LOGGER.log(Level.SEVERE, "An error occurred while loading applet configuration", ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Error closing input stream", ex);
                    }
                }
            }
        }

        if (assetCfg != null){
            settings.putString("AssetConfigURL", assetCfg.toString());
        }

        settings.setWidth(getWidth());
        settings.setHeight(getHeight());

        JmeSystem.setLowPermissions(true);

        try {
            Class<?> clazz = Class.forName(appClass);
            app = (LegacyApplication) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | NoSuchMethodException
                 | IllegalArgumentException
                 | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, "Exception occurred while instantiating " + appClass, ex);
        }

        appToApplet.put(app, this);
        app.setSettings(settings);
        app.createCanvas();

        context = (JmeCanvasContext) app.getContext();
        canvas = context.getCanvas();
        canvas.setSize(getWidth(), getHeight());

        add(canvas);
        app.startCanvas();
    }

    @Override
    public final void update(Graphics g) {
        canvas.setSize(getWidth(), getHeight());
    }

    @Override
    public void init(){
        appClass = getParameter("AppClass");
        if (appClass == null)
            throw new RuntimeException("The required parameter AppClass isn't specified!");

        try {
            appCfg = new URL(getParameter("AppSettingsURL"));
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "Invalid AppSettingsURL parameter", ex);
            appCfg = null;
        }

        try {
            assetCfg = new URL(getParameter("AssetConfigURL"));
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "Invalid AssetConfigURL parameter", ex);
            assetCfg = Resources.getResource("/com/jme3/asset/Desktop.cfg", this.getClass());
        }

        createCanvas();
        LOGGER.info("applet:init");
    }

    @Override
    public void start(){
        context.setAutoFlushFrames(true);
        LOGGER.info("applet:start");
    }

    @Override
    public void stop(){
        context.setAutoFlushFrames(false);
        LOGGER.info("applet:stop");
    }

    @Override
    public void destroy(){
        LOGGER.info("applet:destroyStart");
        SwingUtilities.invokeLater(() -> {
            removeAll();
            LOGGER.info("applet:destroyRemoved");
        });
        app.stop(true);
        LOGGER.info("applet:destroyDone");

        appToApplet.remove(app);
    }

}
