package it.skarafaz.mercury.infrastructure.dagger;

import javax.inject.Singleton;

import dagger.Component;
import it.skarafaz.mercury.MercuryApplication;
import it.skarafaz.mercury.main.MainActivity;

/**
 * Created by letroll on 10/11/16.
 */

@Singleton
@Component(modules = AndroidModule.class)
public interface ApplicationComponent {
    void inject(MercuryApplication application);

    void inject(MainActivity mainActivity);
}