package com.pr0gramm.app.services;

import android.content.Context;
import android.net.Uri;

import com.google.common.collect.ImmutableList;
import com.google.gson.GsonBuilder;
import com.pr0gramm.app.BuildConfig;
import com.pr0gramm.app.Pr0grammApplication;
import com.pr0gramm.app.Settings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.util.async.Async;

/**
 * Class to perform an update check.
 */
public class UpdateChecker {
    private static final Logger logger = LoggerFactory.getLogger(UpdateChecker.class);

    private final int currentVersion;
    private final ImmutableList<String> endpoints;

    public UpdateChecker(Context context) {
        this.currentVersion = Pr0grammApplication.getPackageInfo().versionCode;

        boolean betaChannel = Settings.of(context).useBetaChannel();
        endpoints = updateUrls(betaChannel);
    }

    private Observable<Update> check(String endpoint) {
        return Async.fromCallable(() -> {
            UpdateApi api = newRestAdapter(endpoint).create(UpdateApi.class);
            return api.get().execute().body();

        }, Schedulers.io()).filter(update -> {
            logger.info("Installed v{}, found update v{} at {}",
                    currentVersion, update.version(), endpoint);

            // filter out if up to date
            return update.version() > currentVersion;
        }).map(update -> {
            // rewrite url to make it absolute
            String apk = update.apk();
            if (!apk.startsWith("http")) {
                apk = Uri.withAppendedPath(Uri.parse(endpoint), apk).toString();
            }

            logger.info("Got new update at url " + apk);
            return ImmutableUpdate.builder()
                    .version(update.version())
                    .changelog(update.changelog())
                    .apk(apk)
                    .build();
        });
    }

    public Observable<Update> check() {
        return Observable.from(endpoints)
                .flatMap(ep -> check(ep)
                        .doOnError(err -> logger.warn("Could not check for update: {}", err.toString()))
                        .onErrorResumeNext(Observable.empty()))
                .first();
    }

    private static Retrofit newRestAdapter(String endpoint) {
        com.google.gson.Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(new GsonAdaptersUpdate())
                .create();

        return new Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private interface UpdateApi {
        @GET("update.json")
        Call<Update> get();
    }

    /**
     * Returns the Endpoint-URL that is to be queried
     */
    private static ImmutableList<String> updateUrls(boolean betaChannel) {
        String flavor = BuildConfig.FLAVOR;
        List<String> urls = new ArrayList<>();

        if (betaChannel) {
            urls.add("https://github.com/mopsalarm/pr0gramm-updates/raw/beta/" + flavor + "/");
            urls.add("http://pr0.wibbly-wobbly.de/beta/" + flavor + "/");
        } else {
            urls.add("https://github.com/mopsalarm/pr0gramm-updates/raw/master/" + flavor + "/");
            urls.add("http://pr0.wibbly-wobbly.de/stable/" + flavor + "/");
        }

        return ImmutableList.copyOf(urls);
    }
}
