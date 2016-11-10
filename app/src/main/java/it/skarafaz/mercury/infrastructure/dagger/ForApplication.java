package it.skarafaz.mercury.infrastructure.dagger;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by letroll on 10/11/16.
 */

@Qualifier
@Retention(RUNTIME)
public @interface ForApplication {
}