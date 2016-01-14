package io.github.plastix.forage.data.local;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Module
public class RealmModule {

    @Provides
    public Realm provideRealm() {
        return Realm.getDefaultInstance();
    }
}
