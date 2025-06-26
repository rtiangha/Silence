package org.smssecure.smssecure.dependencies;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = SilenceModule.class)
public interface SilenceComponent {
    void inject(Object object);
}