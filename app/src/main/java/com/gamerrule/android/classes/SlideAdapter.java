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
    private String[] images = {"https://img.freepik.com/free-vector/gradient-gaming-concept-landing-page_23-2149917571.jpg",
            "https://img.freepik.com/free-psd/video-gaming-social-media-promo-template-with-gradient-geometric-forms_23-2149708571.jpg?q=10&h=200",
            "https://img.freepik.com/free-psd/table-hockey-banner-design-template_23-2149221305.jpg?w=2000"};

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
        Glide.with(holder.imageView.getContext())
                .load(images[position])
                .transform(new CenterInside(), new RoundedCorners(24))
                .into(holder.imageView);
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
