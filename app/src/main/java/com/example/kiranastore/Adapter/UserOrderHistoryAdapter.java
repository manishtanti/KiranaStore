package com.example.kiranastore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiranastore.OrderDetailsActivity;
import com.example.kiranastore.R;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserOrderHistoryAdapter extends RecyclerView.Adapter<UserOrderHistoryAdapter.ViewHolder> {
    ArrayList<String> orderList;
    ArrayList<String> orderTimeList;
    Context mContext;
    DatabaseReference ref;
    String userId;

    public UserOrderHistoryAdapter(ArrayList<String> orderList, ArrayList<String> orderTimeList, Context mContext) {
        this.orderList = orderList;
        this.orderTimeList = orderTimeList;
        this.mContext = mContext;
        ref = FirebaseDatabase.getInstance().getReference("ReceivedOrders");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.user_order_histroy_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.orderTime.setText(convertDate(orderTimeList.get(position)));
            holder.orderProductList.setText(orderList.get(position));

            ref.child(orderTimeList.get(position)+userId).child("delivered").addListenerForSingleValueEvent(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists() && snapshot.getValue(Boolean.class)){
                        holder.orderTime.append("\n "+mContext.getString(R.string.delivered));
                        holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected));
                    } else{
                        holder.orderTime.append("\n "+mContext.getString(R.string.mark_undelivered));
                        holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.deselected));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        Intent intent = new Intent(mContext, OrderDetailsActivity.class);
                        intent.putExtra("userId", userId);
                        intent.putExtra("orderId", orderTimeList.get(position));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        mContext.startActivity(intent);
                }
            });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView orderProductList,orderTime;
        public CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderProductList = itemView.findViewById(R.id.order_product_list);
            orderTime = itemView.findViewById(R.id.order_time);
            cardView = itemView.findViewById(R.id.user_history_cardview);
        }
    }

    private String convertDate(String dateInMilliseconds) {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", Long.parseLong(dateInMilliseconds)).toString();
    }
}
