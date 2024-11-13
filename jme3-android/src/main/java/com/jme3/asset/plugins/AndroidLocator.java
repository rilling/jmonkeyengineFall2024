package com.jme3.asset.plugins;

import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import com.jme3.asset.*;
import com.jme3.system.android.JmeAndroidSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class AndroidLocator implements AssetLocator {

    private static final Logger logger = Logger.getLogger(AndroidLocator.class.getName());

    private String rootPath = "";

    public AndroidLocator() {
    }

    @Override
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    public AssetInfo locate(AssetManager manager, AssetKey key) {
        String assetPath = rootPath + key.getName();
        // Fix path issues
        assetPath = assetPath.replace("//", "/");
        if (assetPath.startsWith("/")) {
            // Remove leading /
            assetPath = assetPath.substring(1);
        }
        

        // Not making this a property and storing for future use in case the view stored in JmeAndroidSystem
        // is replaced due to device orientation change.  Not sure it is necessary to do this yet, but am for now.
        android.content.res.Resources androidResources = JmeAndroidSystem.getView().getContext().getResources();
        String androidPackageName = JmeAndroidSystem.getView().getContext().getPackageName();

//        logger.log(Level.INFO, "Asset Key: {0}", key);
//        logger.log(Level.INFO, "Asset Name: {0}", key.getName());
//        logger.log(Level.INFO, "Asset Path: {0}", assetPath);
//        logger.log(Level.INFO, "Asset Extension: {0}", key.getExtension());
//        logger.log(Level.INFO, "Asset Key Class: {0}", key.getClass().getName());
//        logger.log(Level.INFO, "androidPackageName: {0}", androidPackageName);
//        logger.log(Level.INFO, "Resource Name: {0}", getResourceName(assetPath));

        // check the assets directory for the asset using assetPath
        try {
            InputStream in = androidResources.getAssets().open(assetPath);
            if (in != null){
                return new AndroidAssetInfo(manager, key, assetPath, in, 0);
            }
        } catch (IOException ex) {
            // allow to fall through to the other checks in the resources directories.
//            logger.log(Level.INFO, "Resource[{0}] not found in assets directory.", assetPath);
        }

        // if not found in the assets directory, check the drawable and mipmap directories (only valid for images)
        String resourceName = getResourceName(assetPath);
        int resourceId = androidResources.getIdentifier(resourceName, "drawable", androidPackageName);
//        logger.log(Level.INFO, "drawable resourceId: {0}", resourceId);
        if (resourceId == 0) { // drawable resource not found, check mipmap resource type
            resourceId = androidResources.getIdentifier(resourceName, "mipmap", androidPackageName);
//            logger.log(Level.INFO, "mipmap resourceId: {0}", resourceId);
        }
        if (resourceId == 0) {  // not found in resource directories, return null;
            return null;
        }

        // try to open a stream with the resourceId returned by Android
        try {
            InputStream in = androidResources.openRawResource(resourceId);
            if (in != null){
//                logger.log(Level.INFO, "Creating new AndroidResourceInfo.");
                return new AndroidAssetInfo(manager, key, assetPath, in, resourceId);
            }
        } catch (Resources.NotFoundException ex) {
            // input stream failed to open, return null
            return null;
        }

        return null;
    }



    public class AndroidAssetInfo extends AssetInfo {

        private InputStream in;
        private final String assetPath;
        private int resourceId;
        private static final String AssetOpenError="Failed to open asset";
        AndroidAssetInfo(AssetManager assetManager, AssetKey<?> key, String assetPath, InputStream in, int resourceId) {
            super(assetManager, key);
            this.assetPath = assetPath;
            this.in = in;
            this.resourceId = resourceId;
        }

        private android.content.res.Resources getAndroidResources() {
            return JmeAndroidSystem.getView().getContext().getResources();
        }

        private InputStream getResourceAsStream(String assetPath, int resourceId) throws AssetLoadException {
            android.content.res.Resources androidResources = getAndroidResources();
            try {
                if (resourceId == 0) {
                    return androidResources.getAssets().open(assetPath);
                } else {
                    return androidResources.openRawResource(resourceId);
                }
            } catch (IOException | Resources.NotFoundException ex) {
                throw new AssetLoadException("Failed to open asset " + assetPath, ex);
            }
        }

        @Override
        public InputStream openStream() {
            if (in != null) {
                InputStream in2 = in;
                in = null;
                return in2;
            } else {
                return getResourceAsStream(assetPath, resourceId);
            }
        }

        public AssetFileDescriptor openFileDescriptor() {
            android.content.res.Resources androidResources = getAndroidResources();
            try {
                if (resourceId == 0) {
                    return androidResources.getAssets().openFd(assetPath);
                } else {
                    return androidResources.openRawResourceFd(resourceId);
                }
            } catch (IOException | Resources.NotFoundException ex) {
                throw new AssetLoadException("Failed to open asset " + assetPath, ex);
            }
        }
    }

    private String getResourceName(String name) {
        int idx = name.lastIndexOf('.');
        if (idx <= 0 || idx == name.length() - 1) {
            return name;
        } else {
            return name.substring(0, idx).toLowerCase();
        }
    }
}
