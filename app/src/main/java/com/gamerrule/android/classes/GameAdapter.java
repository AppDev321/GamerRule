package com.gamerrule.android.classes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.gamerrule.android.R;

import java.util.ArrayList;
import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    private List<Game> games = new ArrayList<>();

    public void setGames(List<Game> games) {
        this.games = games;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);

        holder.gameNameTextView.setText(game.getGameName());

        String imageUrl = game.getImageURL();
//        Log.e("GameAdapter", "Image URL is null for game: " + game.getGameName());
//        Log.e("GameAdapter", "Image URL is null for game: " + game.getImageURL());
        if (imageUrl != null) {
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .transform(new CenterCrop(), new RoundedCorners(8))
                    .into(holder.gameImageView);
        } else {
            // Handle the case where the image URL is null or invalid
            // For example, you can set a placeholder image or show an error message
            holder.gameImageView.setImageResource(R.drawable.round_button_primary);
        }

        holder.itemView.setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(game);
            }
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameImageView;
        TextView gameNameTextView;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImageView = itemView.findViewById(R.id.imageView_game_item);
            gameNameTextView = itemView.findViewById(R.id.textView_game_item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Game game);
    }
    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}