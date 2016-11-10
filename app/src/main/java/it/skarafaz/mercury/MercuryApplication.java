package it.skarafaz.mercury;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import it.skarafaz.mercury.infrastructure.dagger.AndroidModule;
import it.skarafaz.mercury.infrastructure.dagger.ApplicationComponent;
import it.skarafaz.mercury.infrastructure.dagger.DaggerApplicationComponent;

public class MercuryApplication extends Application {
    private static final String S_HAS_PERMANENT_MENU_KEY = "sHasPermanentMenuKey";
    private static final Logger logger = LoggerFactory.getLogger(MercuryApplication.class);
    private static Context context;
    @Inject
    LocationManager locationManager; // for some reason.
    private ApplicationComponent component;

    public static Context getContext() {
        return context;
    }

    public static boolean storagePermissionGranted() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public ApplicationComponent component() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        component = DaggerApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
        component().inject(this); // As of now, LocationManager should be injected into this.

        EventBus.builder().addIndex(new EventBusIndex()).build();
    }
}
