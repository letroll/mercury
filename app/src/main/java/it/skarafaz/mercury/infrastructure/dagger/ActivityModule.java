package it.skarafaz.mercury.infrastructure.dagger;

/**
 * Created by letroll on 10/11/16.
 */

import android.app.Activity;

import dagger.Module;
import dagger.Provides;

/**
 * A module to wrap the Activity state and expose it to the graph.
 */
@Module
public class ActivityModule {
    private final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    /**
     * Expose the activity to dependents in the graph.
     */
    @Provides
    @ForActivity
    Activity activity() {
        return activity;
    }
}