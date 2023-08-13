package com.gamerrule.android.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamerrule.android.R;
import com.gamerrule.android.ui.users.TransactionsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<User> userList;

    private UsersAdapter.OnTransactionButtonClickListener transactionButtonClickListener;

    public interface OnTransactionButtonClickListener {
        void onTransactionButtonClick(int position);
    }

    public UsersAdapter(List<User> userList) {
        this.userList = userList;
    }


    public void setUserList(List<User> userList) {
        this.userList = userList;
        notifyDataSetChanged();
    }
    public void setTransactionButtonClickListener(UsersAdapter.OnTransactionButtonClickListener listener) {
        this.transactionButtonClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.bindUser(user);

        holder.transactionsButton.setOnClickListener(v -> {
            if (transactionButtonClickListener != null) {
                transactionButtonClickListener.onTransactionButtonClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewEmail;
        private TextView textViewPhone;
        private TextView textViewWalletBalance;
        private TextView textViewMatchesPlayed;
        private Button transactionsButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views from item_user.xml
            textViewName = itemView.findViewById(R.id.textViewName_user_item);
            textViewEmail = itemView.findViewById(R.id.textViewEmail_user_item);
            textViewPhone = itemView.findViewById(R.id.textViewPhone_user_item);
            textViewWalletBalance = itemView.findViewById(R.id.textViewWalletBalance_user_item);
            textViewMatchesPlayed = itemView.findViewById(R.id.textViewMatchesPlayed_user_item);
            transactionsButton = itemView.findViewById(R.id.buttonViewTransactions_user_item);

        }

        public void bindUser(User user) {
            // Bind user data to the views
            textViewName.setText(user.getName());
            textViewEmail.setText(user.getEmail());
            textViewPhone.setText(user.getPhone());


            // Get wallet balance
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference walletRef = db.collection("wallets").document(user.getUid());
            walletRef.get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                // Wallet document found
                                double balance = documentSnapshot.getDouble("balance");

                                textViewWalletBalance.setText("Wallet Balance: " + balance);
                            } else {
                                // Wallet document not found
                                textViewWalletBalance.setText("Wallet Balance: " + 0.00);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error fetching wallet balance
                            textViewWalletBalance.setText("Wallet Balance: " + "Error");
                        }
                    });


            // Get number of matches played
            CollectionReference participantsRef = db.collection("participants");
            Query query = participantsRef.whereEqualTo("paticipatedUserId", user.getUid());
            query.get().addOnSuccessListener(querySnapshot -> {
                int matchesPlayed = querySnapshot.size();
                textViewMatchesPlayed.setText("Matches Participated: " + matchesPlayed);
            }).addOnFailureListener(e -> {
                textViewMatchesPlayed.setText("Matches Participated: " + "Error fetching");

            });

        }
    }

    public interface OnTransactionClickListener {
        void onTransactionClick(User user);
    }
}

