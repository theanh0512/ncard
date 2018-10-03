/*
 *  Copyright (C) 2017 Bilibili
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.user.ncard.ui.catalogue.utils;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bilibili.boxing.loader.IBoxingCallback;
import com.bilibili.boxing.loader.IBoxingMediaLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.user.ncard.R;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.bumptech.glide.request.RequestOptions.centerCropTransform;
import static com.bumptech.glide.request.RequestOptions.centerInsideTransform;


/**
 * Created by trong-android-dev on 1/11/17.
 */
public class GlideHelper {

    @BindingAdapter("app:avatarUrl")
    public static void displayAvatar(@NonNull ImageView img, @Nullable String url) {
        String path = url;
        try {
            // https://github.com/bumptech/glide/issues/1531
            /*Glide.with(img.getContext())
                    .load(path)
                    .placeholder(R.drawable.ic_boxing_default_image).crossFade().centerCrop().override(width, height).into(img);*/
            RequestOptions requestOptions = centerCropTransform()
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .apply(RequestOptions.circleCropTransform())
                    .override(100)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH);
            // https://github.com/bumptech/glide/issues/1026
            Glide.with(img.getContext())
                    .load(path)
                    .apply(requestOptions)
                    .into(img);
        } catch (IllegalArgumentException ignore) {
        }
    }

    @BindingAdapter("app:largeAvatarUrl")
    public static void displayLargeAvatar(@NonNull ImageView img, @Nullable String url) {
        String path = url;
        try {
            // https://github.com/bumptech/glide/issues/1531
            /*Glide.with(img.getContext())
                    .load(path)
                    .placeholder(R.drawable.ic_boxing_default_image).crossFade().centerCrop().override(width, height).into(img);*/
            RequestOptions requestOptions = centerCropTransform()
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .apply(RequestOptions.circleCropTransform())
                    .override(400)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH);
            // https://github.com/bumptech/glide/issues/1026
            Glide.with(img.getContext())
                    .load(path)
                    .apply(requestOptions)
                    .into(img);
        } catch (IllegalArgumentException ignore) {
        }
    }

    @BindingAdapter("app:imageUrl")
    public static void displayRaw(@NonNull final ImageView img, @Nullable String url) {
        String path = url;

        RequestOptions requestOptions = centerCropTransform()
//                .placeholder(R.drawable.ic_boxing_default_image)
//                .error(R.drawable.ic_boxing_default_image)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);

        RequestBuilder<Drawable> bitmapRequestBuilder = Glide.with(img.getContext()).asDrawable().load(path);
        bitmapRequestBuilder.apply(requestOptions);
        bitmapRequestBuilder.listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(img);
    }

    public static void displayThumbnail(@NonNull ImageView img, @Nullable String absPath, int width, int height) {
        String path = absPath;
        try {

            RequestOptions requestOptions = centerCropTransform()
                    .placeholder(R.drawable.ic_boxing_default_image)
                    .error(R.drawable.ic_boxing_default_image)
                    .override(width, height)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH);

            Glide.with(img.getContext())
                    .load(path)
                    .apply(requestOptions)
                    .into(img);
        } catch (IllegalArgumentException ignore) {
        }

    }

    public static void displayRaw(@NonNull final ImageView img, @Nullable String absPath, int width, int height, final ImageLoadingListener callback) {
        String path = absPath;

        RequestOptions requestOptions = centerInsideTransform()
//                .placeholder(R.drawable.ic_boxing_default_image)
//                .error(R.drawable.ic_boxing_default_image)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(width, height);

        RequestBuilder<Drawable> bitmapRequestBuilder = Glide.with(img.getContext()).asDrawable().load(path);
        if (width > 0 && height > 0) {
            requestOptions.override(width, height);
        }
        bitmapRequestBuilder.apply(requestOptions);
        bitmapRequestBuilder.listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (callback != null) {
                    callback.onFailed(e);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (resource != null && callback != null) {
                    img.setImageDrawable(resource);
                    callback.onLoaded();
                    return true;
                }
                return false;
            }
        }).into(img);
    }

    public static void displayRawCenterCrop(@NonNull final ImageView img, @Nullable String absPath, int width, int height, final ImageLoadingListener callback) {
        String path = absPath;

        RequestOptions requestOptions = centerCropTransform()
                .placeholder(R.drawable.ic_boxing_default_image)
                .error(R.drawable.ic_boxing_default_image)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .override(width, height);

        RequestBuilder<Drawable> bitmapRequestBuilder = Glide.with(img.getContext()).asDrawable().load(path);
        if (width > 0 && height > 0) {
            requestOptions.override(width, height);
        }
        bitmapRequestBuilder.apply(requestOptions);
        bitmapRequestBuilder.listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if (callback != null) {
                    callback.onFailed(e);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                if (resource != null && callback != null) {
                    img.setImageDrawable(resource);
                    callback.onLoaded();
                    return true;
                }
                return false;
            }
        }).into(img);
    }

    public static void displaySquareAvatar(@NonNull ImageView img, @Nullable String url) {
        String path = url;
        try {
            RequestOptions requestOptions = centerCropTransform()
                    .placeholder(R.drawable.avatar_default)
                    .error(R.drawable.avatar_default)
                    .override(100)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .priority(Priority.HIGH);

            Glide.with(img.getContext())
                    .load(path)
                    .apply(requestOptions)
                    .into(img);
        } catch (IllegalArgumentException ignore) {
        }
    }


    public interface ImageLoadingListener {
        void onLoaded();

        void onFailed(GlideException e);
    }

}
