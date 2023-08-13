package com.gamerrule.android.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.gamerrule.android.R;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {
    private int[] images = {
            R.drawable.slider_image_1,
            R.drawable.slider_image_2,
            R.drawable.slider_image_3
    };

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide, parent, false);
        return new SlideViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        // Load image using Glide and apply rounded corners transformation
        RequestOptions requestOptions = new RequestOptions()
                .transform(new RoundedCorners(16)); // Set corner radius here
        holder.imageView.setClipToOutline(true);
        holder.imageView.setImageDrawable(holder.imageView.getResources().getDrawable(images[position]));
//        Glide.with(holder.imageView.getContext())
//                .load()
//                .transform(new CenterInside(), new RoundedCorners(24))
//                .into(holder.imageView);
        holder.imageView.setClipToOutline(true);
    }

    @Override
    public int getItemCount() {
        return images.length;
    }

    class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView_item_slide);
        }
    }
}
