package com.user.ncard.di.network;

import java.io.IOException;

import javax.inject.Singleton;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An interceptor that allows runtime changes to the API Base URL in Retrofit.
 * The Base URL is set by calling the {@link ChangeableBaseUrlInterceptor#setInterceptor(String)} method.
 */
@Singleton
public class ChangeableBaseUrlInterceptor implements Interceptor {
    private static ChangeableBaseUrlInterceptor sInterceptor;
    private String mScheme;
    private String mHost;

    public ChangeableBaseUrlInterceptor() {
        // Intentionally blank
    }

    public void setInterceptor(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        mScheme = httpUrl.scheme();
        mHost = httpUrl.host();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // If new Base URL is properly formatted than replace with old one
        if (mScheme != null && mHost != null) {
            HttpUrl newUrl = original.url().newBuilder()
                    .scheme(mScheme)
                    .host(mHost)
                    .build();
            /*String newUrlString = newUrl.toString();
            if (newUrlString.contains("bcr1.intsig.net")) {
                newUrlString = newUrlString.replace("dev/", "");
                newUrl = HttpUrl.parse(newUrlString);
            }*/
            original = original.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(original);
    }
}
