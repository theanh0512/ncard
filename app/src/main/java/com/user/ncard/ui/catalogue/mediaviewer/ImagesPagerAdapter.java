package com.user.ncard.ui.catalogue.mediaviewer;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.user.ncard.R;
import com.user.ncard.ui.catalogue.main.Media;
import com.user.ncard.ui.catalogue.utils.GlideHelper;

import java.util.ArrayList;
import java.util.List;

public class ImagesPagerAdapter extends RecyclePagerAdapter<ImagesPagerAdapter.ViewHolder> {

    private static final long PROGRESS_DELAY = 300L;

    private ViewPager viewPager;
    private List<Media> medias;

    private boolean activated;

    public ImagesPagerAdapter(ViewPager pager, List<Media> medias) {
        this.viewPager = pager;
        this.medias = medias;
    }

    public void setImages(List<Media> medias) {
        this.medias = medias;
        notifyDataSetChanged();
    }

    /**
     * To prevent ViewPager from holding heavy views (with bitmaps)  while it is not showing
     * we may just pretend there are no items in this adapter ("activate" = false).
     * But once we need to run opening animation we should "activate" this adapter again.<br/>
     * Adapter is not activated by default.
     */
    public void setActivated(boolean activated) {
        if (this.activated != activated) {
            this.activated = activated;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return !activated || medias == null ? 0 : medias.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
        final ViewHolder holder = new ViewHolder(container);
        holder.image.getController().getSettings()
                .setMaxZoom(10f)
                .setDoubleTapZoom(3f);

        holder.image.getController().enableScrollInViewPager(viewPager);
        holder.image.getPositionAnimator().addPositionUpdateListener(new ViewPositionAnimator.PositionUpdateListener() {
            @Override
            public void onPositionUpdate(float position, boolean isLeaving) {
                holder.progress.setVisibility(position == 1f ? View.VISIBLE : View.INVISIBLE);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //GlideHelper.displayRaw(holder.image, medias.get(position).getPath(), 0, 0, null);

        // Temporary disabling touch controls
        if (!holder.gesturesDisabled) {
            holder.image.getController().getSettings().disableGestures();
            holder.gesturesDisabled = true;
        }

        holder.progress.animate().setStartDelay(PROGRESS_DELAY).alpha(1f);


        // Loading image
        GlideHelper.displayRaw(holder.image, medias.get(position).getPath(), 0, 0, new GlideHelper.ImageLoadingListener() {
            @Override
            public void onLoaded() {
                holder.progress.animate().cancel();
                holder.progress.animate().alpha(0f);
                // Re-enabling touch controls
                if (holder.gesturesDisabled) {
                    holder.image.getController().getSettings().enableGestures();
                    holder.gesturesDisabled = false;
                }
            }

            @Override
            public void onFailed(GlideException e) {
                holder.progress.animate().alpha(0f);
            }
        });
    }

    @Override
    public void onRecycleViewHolder(@NonNull ViewHolder holder) {
        super.onRecycleViewHolder(holder);

        if (holder.gesturesDisabled) {
            holder.image.getController().getSettings().enableGestures();
            holder.gesturesDisabled = false;
        }

        holder.progress.animate().cancel();
        holder.progress.setAlpha(0f);

        holder.image.setImageDrawable(null);
    }

    public static GestureImageView getImage(RecyclePagerAdapter.ViewHolder holder) {
        return ((ViewHolder) holder).image;
    }

    public static class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        final GestureImageView image;
        final View progress;

        boolean gesturesDisabled;

        ViewHolder(ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_full, parent, false));
            image = itemView.findViewById(R.id.photo_full_image);
            progress = itemView.findViewById(R.id.photo_full_progress);
        }
    }

}
