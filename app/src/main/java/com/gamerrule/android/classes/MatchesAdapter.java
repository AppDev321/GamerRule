package com.gamerrule.android.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerrule.android.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {

    private List<Match> matchesList;
    private List<Participant> participantsList;
    private boolean isAdmin;
    private OnJoinButtonClickListener joinButtonClickListener;
    private OnButtonClickListener buttonClickListener;

    private String currentUserId;
    private Context context;
    private ProgressDialog progressDialog;

    public interface OnJoinButtonClickListener {
        void onJoinButtonClick(int position);
    }
    public interface OnButtonClickListener {
        void onEditButtonClick(int position);
        void onDeleteButtonClick(int position);
        void onRewardsButtonClick(int position);
        void onViewParticipantsButtonClick(int position);
        void onViewRoomIdButtonClick(int position);
    }


    public MatchesAdapter(List<Match> matchesList, boolean isAdmin, String currentUid, Context context) {
        this.matchesList = matchesList;
        this.isAdmin = new Constants().isUserAdmin();
        this.currentUserId = currentUid;
        this.context = context;
    }




    public void setMatches(List<Match> matches) {
        this.matchesList = matches;
        notifyDataSetChanged();
    }

    public Match getMatch(int position) {
        if (position < getItemCount())
            return matchesList.get(position);
        return null;
    }

    public void setJoinButtonClickListener(OnJoinButtonClickListener listener) {
        this.joinButtonClickListener = listener;
    }

    public void setButtonClickListener(OnButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
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

        getParticipantIds(match, holder);

        holder.joinButton.setOnClickListener(v -> {
            if (joinButtonClickListener != null) {
                joinButtonClickListener.onJoinButtonClick(position);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onEditButtonClick(position);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onDeleteButtonClick(position);
            }
        });

        holder.rewardsButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onRewardsButtonClick(position);
            }
        });

        holder.participantsButton.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onViewParticipantsButtonClick(position);
            }
        });

        holder.viewParticipant.setOnClickListener(v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onViewParticipantsButtonClick(position);
            }
        });

        holder.viewRoomIdAndPass.setOnClickListener( v -> {
            if (buttonClickListener != null) {
                buttonClickListener.onViewRoomIdButtonClick(position);
            }
        });

    }

    private void getParticipantIds(Match match, MatchViewHolder holder) {
        String matchId = match.getDocumentId();
        CollectionReference participantsRef = FirebaseFirestore.getInstance().collection("participants");

        Query query = participantsRef.whereEqualTo("matchId", matchId);

        showProgressDialog(holder);

        query.get().addOnSuccessListener(querySnapshot -> {
            participantsList = new ArrayList<>();
            int progress = 0;

            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Participant participant = document.toObject(Participant.class);
                if (participant != null) {
                    participantsList.add(participant);

                    String participantUserId = participant.getPaticipatedUserId();
                    if (participantUserId != null && participantUserId.equals(currentUserId)) {
                        holder.joinButton.setEnabled(false);
                        holder.joinButton.setText("Joined");
                        holder.joinButton.setBackgroundResource(R.drawable.round_button_disabled);

                        if(!match.getMatchStatus().equals("Upcoming")){
                            holder.llOngoingOnly.setVisibility(View.VISIBLE);
                        }
                    }

                    progress++;
                }
            }

            if(!match.getMatchStatus().equals("Upcoming") && holder.joinButton.isEnabled()){
                holder.joinButton.setVisibility(View.GONE);
            }

            holder.tvParticipated.setText("Participated: " + progress);
            holder.tvRemaining.setText("Maximum: " + match.getMaxPlayers());

            hideProgressDialog(holder);

            int progressbarprogress = progress * 100 / match.getMaxPlayers();
            holder.participantProgress.setProgress(progressbarprogress);

        }).addOnFailureListener(e -> {
            // Error occurred while retrieving the participants
            // Handle the error
            hideProgressDialog(holder);
        });
    }

    private void showProgressDialog(MatchViewHolder holder) {
//        if (context != null && !((Activity) context).isFinishing() && !(progressDialog != null && progressDialog.isShowing())) {
//            progressDialog = new ProgressDialog(context);
//            progressDialog.setMessage("Loading...");
//            progressDialog.setCancelable(false);
//            progressDialog.show();
//        }

        holder.participantProgress.setIndeterminate(true);
    }


    private void hideProgressDialog(MatchViewHolder holder) {
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
        holder.participantProgress.setIndeterminate(false);
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
        private TextView tvParticipated;
        private TextView tvRemaining;
        private ImageButton ibEditMatchButton;
        private TextView joinButton;
        private ProgressBar participantProgress;
        private ImageButton editButton;
        private ImageButton deleteButton;
        private ImageButton participantsButton;
        private ImageButton rewardsButton;
        private LinearLayout llAdminActions, llOngoingOnly;
        private Button viewRoomIdAndPass, viewParticipant;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from item_match.xml
            textViewDate = itemView.findViewById(R.id.tvDateValue);
            textViewTime = itemView.findViewById(R.id.tvTimeValue);
            textViewMap = itemView.findViewById(R.id.tvMapValue);
            textViewType = itemView.findViewById(R.id.tvMatchTypeValue);
            tvTitleMatch = itemView.findViewById(R.id.tv_title_match);

            textViewPrizePool = itemView.findViewById(R.id.tvPrizePoolValue);
            textViewPerKill = itemView.findViewById(R.id.tvPerKillValue);
            textViewEntryFees = itemView.findViewById(R.id.tvEntryFeesValue);

            joinButton = itemView.findViewById(R.id.btnJoin);
            participantProgress = itemView.findViewById(R.id.progressBar);
            tvParticipated = itemView.findViewById(R.id.tvParticipated);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);

            llAdminActions = itemView.findViewById(R.id.linear_layout_admin_only_match_item);
            editButton = itemView.findViewById(R.id.ib_edit_match_item);
            deleteButton = itemView.findViewById(R.id.ib_delete_match_item);
            participantsButton = itemView.findViewById(R.id.ib_participants_match_item);
            rewardsButton = itemView.findViewById(R.id.ib_rewards_match_item);

            llOngoingOnly = itemView.findViewById(R.id.ongoing_only_layout_item_match);
            viewParticipant = itemView.findViewById(R.id.view_participants_match_item);
            viewRoomIdAndPass = itemView.findViewById(R.id.view_room_id_match_item);

        }

        public void bindMatch(Match match) {
            // Bind match data to the views
            // Format the date and time separately using SimpleDateFormat
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa");

            tvTitleMatch.setText(match.getImageUrl());
            textViewDate.setText(dateFormat.format(match.getMatchSchedule()));
            textViewTime.setText(timeFormat.format(match.getMatchSchedule()));
            textViewMap.setText(match.getMap());
            textViewType.setText(match.getMatchType());

            textViewPrizePool.setText(match.getPrizePool());
            textViewPerKill.setText(match.getPerKill());
            textViewEntryFees.setText(match.getEntryFees());

            if (isAdmin) {
                llAdminActions.setVisibility(View.VISIBLE);
            } else {
                llAdminActions.setVisibility(View.GONE);
            }

            if(!match.getMatchStatus().equals("Upcoming") && (isAdmin)){
                llOngoingOnly.setVisibility(View.VISIBLE);
            }else{
                llOngoingOnly.setVisibility(View.GONE);
            }
        }
    }
}