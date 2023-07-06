package com.gamerrule.android.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerrule.android.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<Match> matchesList;
    private boolean isAdmin;

    public MatchesAdapter(List<Match> matchesList, boolean isAdmin) {
        this.matchesList = matchesList;
        this.isAdmin = isAdmin;
    }

    public void setMatches(List<Match> matches) {
        this.matchesList = matches;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        Match match = matchesList.get(position);

        // Bind match data to the views in the MatchViewHolder
        holder.bindMatch(match);
    }

    @Override
    public int getItemCount() {
        return matchesList.size();
    }

    public class MatchViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewDate;
        private TextView tvTitleMatch;
        private TextView textViewTime;
        private TextView textViewMap;
        private TextView textViewType;

        private TextView textViewPrizePool;
        private TextView textViewPerKill;
        private TextView textViewEntryFees;
        private ImageButton ibEditMatchButton;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from item_match.xml
            textViewDate = itemView.findViewById(R.id.tvDateValue);
            textViewTime = itemView.findViewById(R.id.tvTimeValue);
            textViewMap = itemView.findViewById(R.id.tvMapValue);
            textViewType = itemView.findViewById(R.id.tvMatchTypeValue);
            tvTitleMatch = itemView.findViewById(R.id.tv_title_match);
            ibEditMatchButton = itemView.findViewById(R.id.ibEditMatchButton);

            textViewPrizePool = itemView.findViewById(R.id.tvPrizePoolValue);
            textViewPerKill = itemView.findViewById(R.id.tvPerKillValue);
            textViewEntryFees = itemView.findViewById(R.id.tvEntryFeesValue);
        }

        public void bindMatch(Match match) {
            // Bind match data to the views
            // Format the date and time separately using SimpleDateFormat
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            tvTitleMatch.setText(match.getImageUrl());
            textViewDate.setText(dateFormat.format(match.getMatchSchedule()));
            textViewTime.setText(timeFormat.format(match.getMatchSchedule()));
            textViewMap.setText(match.getMap());
            textViewType.setText(match.getMatchType());

            textViewPrizePool.setText(match.getPrizePool());
            textViewPerKill.setText(match.getPerKill());
            textViewEntryFees.setText(match.getEntryFees());

            if(isAdmin){
                ibEditMatchButton.setVisibility(View.VISIBLE);
                ibEditMatchButton.setEnabled(true);
            }else{
                ibEditMatchButton.setVisibility(View.GONE);
                ibEditMatchButton.setEnabled(false);
            }
        }
    }
}

