package com.gamerrule.android.classes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private OnEditClickListener onEditClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnItemClickListener onItemClickListener;
    private boolean isAdmin ;

    public void setGames(List<Game> games) {
        this.games = games;
        isAdmin = new Constants().isUserAdmin();
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
        // Log.e("GameAdapter", "Image URL is null for game: " + game.getGameName());

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

        holder.editButton.setOnClickListener(view -> {
            if (onEditClickListener != null) {
                onEditClickListener.onEditClick(game);
            }
        });

        holder.deleteButton.setOnClickListener(view -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(game);
            }
        });

        if(isAdmin){
            holder.linearLayoutAdminOnly.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameImageView;
        TextView gameNameTextView;

        ImageButton editButton, deleteButton;
        LinearLayout linearLayoutAdminOnly;

        GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameImageView = itemView.findViewById(R.id.imageView_game_item);
            gameNameTextView = itemView.findViewById(R.id.textView_game_item);
            editButton = itemView.findViewById(R.id.ib_edit_game_item);
            deleteButton = itemView.findViewById(R.id.ib_delete_game_item);
            linearLayoutAdminOnly = itemView.findViewById(R.id.linear_layout_admin_only_game_item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Game game);
    }
    public interface OnEditClickListener {
        void onEditClick(Game game);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Game game);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


    public void setOnEditClickListener(OnEditClickListener listener) {
        this.onEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

}