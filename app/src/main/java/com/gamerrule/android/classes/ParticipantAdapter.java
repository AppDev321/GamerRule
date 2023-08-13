package com.gamerrule.android.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerrule.android.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder> {
    private List<Participant> participants;
    private OnParticipantButtonClickListener buttonClickListener;
    private FirebaseFirestore firestore;
    private boolean isAdmin;

    public ParticipantAdapter() {
        this.participants = new ArrayList<>();
        this.firestore = FirebaseFirestore.getInstance();
        isAdmin = new Constants().isUserAdmin();
    }

    public Participant getParticipant(int position){
        return participants.get(position);
    }
    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
        notifyDataSetChanged();
    }

    public void setButtonClickListener(OnParticipantButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_participant, parent, false);
        return new ParticipantViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ParticipantViewHolder holder, int position) {
        Participant participant = participants.get(position);
        holder.bindParticipant(participant);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public class ParticipantViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewGameUsername;
        private TextView textViewParticipantName;
        private TextView textViewWalletBalance;
        private TextView textViewParticipantEmail;
        private TextView textViewParticipantNumber;
        private TextView textViewKillNumber;
        private TextView textViewRank;
        private TextView textViewAmountWin;
        private Button buttonGiveReward;
        private Button buttonSeeTransactions;
        private User selectedUser;

        private LinearLayout parent;
        private LinearLayout adminOnly;

        public ParticipantViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewGameUsername = itemView.findViewById(R.id.textViewGameUsername_participant_item);
            textViewParticipantName = itemView.findViewById(R.id.textViewParticipantName_participant_item);
            textViewWalletBalance = itemView.findViewById(R.id.textViewWalletBalance_participant_item);
            textViewParticipantEmail = itemView.findViewById(R.id.textViewParticipantEmail_participant_item);
            textViewParticipantNumber = itemView.findViewById(R.id.textViewParticipantNumber_participant_item);
            textViewKillNumber = itemView.findViewById(R.id.textViewKillNumber_participant_item);
            textViewRank = itemView.findViewById(R.id.textViewRank_participant_item);
            textViewAmountWin = itemView.findViewById(R.id.textViewAmountWin_participant_item);
            buttonGiveReward = itemView.findViewById(R.id.buttonGiveReward_participant_item);
            buttonSeeTransactions = itemView.findViewById(R.id.buttonSeeTransactions_participant_item);
            parent = itemView.findViewById(R.id.ll_parent_item_participant);
            adminOnly = itemView.findViewById(R.id.ll_admin_only_participant_item);

            if(isAdmin){
                adminOnly.setVisibility(View.VISIBLE);
            }

            buttonGiveReward.setOnClickListener(v -> {
                if (buttonClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        buttonClickListener.onGiveRewardButtonClick(position);
                    }
                }
            });

            buttonSeeTransactions.setOnClickListener(v -> {
                if (buttonClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        buttonClickListener.onSeeTransactionsButtonClick(position);
                    }
                }
            });
        }

        public void bindParticipant(Participant participant) {
            textViewGameUsername.setText(participant.getGameUsername());
            textViewParticipantName.setText("");
            textViewParticipantEmail.setText("");
            textViewParticipantNumber.setText("");
            textViewKillNumber.setText("Kills: "+participant.getKills());
            textViewRank.setText("Rank: "+participant.getRank());
            textViewAmountWin.setText("Winning Amount: "+participant.getWinningAmount());

            if (participant.isRewarded()) {
                // Set the background to a rewarded color
                parent.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.reward_background));
            }
            String userId = participant.getPaticipatedUserId();

            // Retrieve wallet balance from "wallets" collection

            DocumentReference walletRef = firestore.collection("wallets").document(userId);
            walletRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    double balance = documentSnapshot.getDouble("balance");
                    textViewWalletBalance.setText("Wallet Balance: " + (balance));
                }
            }).addOnFailureListener(e -> {
                // Handle failure
                textViewWalletBalance.setText("Wallet Balance: "+"N/A");
            });

            // Retrieve participant email and number from "users" collection
            DocumentReference userRef = firestore.collection("users").document(userId);
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    selectedUser = documentSnapshot.toObject(User.class);
                    textViewParticipantEmail.setText("Participant Email: "+selectedUser.getEmail());
                    textViewParticipantNumber.setText("Participant Phone: "+selectedUser.getPhone());
                    textViewParticipantName.setText("Participant Name: "+selectedUser.getName());
                }
            }).addOnFailureListener(e -> {
                // Handle failure
                textViewParticipantEmail.setText("Participant Email: "+"N/A");
                textViewParticipantNumber.setText("Participant Phone: "+"N/A");
                textViewParticipantName.setText("Participant Name: "+"N/A");
            });
        }
    }

    public interface OnParticipantButtonClickListener {
        void onGiveRewardButtonClick(int position);
        void onSeeTransactionsButtonClick(int position);
    }
}
