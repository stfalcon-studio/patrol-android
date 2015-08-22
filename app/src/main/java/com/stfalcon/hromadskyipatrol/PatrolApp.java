package com.stfalcon.hromadskyipatrol;

import android.app.Application;
import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;
import io.fabric.sdk.android.Fabric;



/**
 * Created by alexandr on 22/08/15.
 */
public class PatrolApp extends Application {

    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        initImageLoader();
    }


    private void initImageLoader() {

        // init image loader
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .resetViewBeforeLoading()
                .cacheInMemory(false)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(400))
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .diskCacheExtraOptions(480, 320, null)
                .threadPoolSize(2)
                .threadPriority(Thread.MIN_PRIORITY + 2)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator())
                .imageDownloader(new BaseImageDownloader(this))
                        // connectTimeout (5 s), readTimeout (30 s)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        L.disableLogging();
        ImageLoader.getInstance().init(config);
    }


    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        try {
            if (mTracker == null) {
                GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
                // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
                mTracker = analytics.newTracker("UA-44978148-14");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return mTracker;
    }
}
