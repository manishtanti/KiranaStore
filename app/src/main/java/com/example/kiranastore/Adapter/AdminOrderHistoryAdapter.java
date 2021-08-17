package com.example.kiranastore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiranastore.Model.Order;
import com.example.kiranastore.Model.User;
import com.example.kiranastore.OrderDetailsActivity;
import com.example.kiranastore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdminOrderHistoryAdapter extends RecyclerView.Adapter<AdminOrderHistoryAdapter.ViewHolder> {
    private ArrayList<Order> receivedOrderList;
    private Context mContext;
    private DatabaseReference reference,orderReference;

    public AdminOrderHistoryAdapter(ArrayList<Order> receivedOrderList, Context mContext) {
        this.receivedOrderList = receivedOrderList;
        this.mContext = mContext;
        reference = FirebaseDatabase.getInstance().getReference("Users");
        orderReference = FirebaseDatabase.getInstance().getReference("ReceivedOrders");

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.admin_order_history_item,parent,false));
    }


   
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = receivedOrderList.get(position);
        holder.orderTime.setText(convertDate(order.getOrderId()));
        holder.itemCount.setText(mContext.getString(R.string.total_items) +order.getItemCount());
        holder.totalPrice.setText(mContext.getString(R.string.total_rs)+order.getPrice());

        if(order.isDelivered()){
            holder.orderDelivered.setText(mContext.getString(R.string.mark_undelivered));
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.selected,null));
        } else{
            holder.orderDelivered.setText(mContext.getString(R.string.mark_delivered));
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.deselected,null));
        }
        reference.child(order.getCustomerId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if(user!=null) {
                    if (user.getImageUrl().equals("default")) {
                        holder.customerImage.setImageResource(R.drawable.ic_person);
                    } else {
                        Picasso.get().load(user.getImageUrl()).into(holder.customerImage);
                    }
                    holder.customerName.setText(user.getFullName());
                    holder.customerPhoneNo.setText(mContext.getString(R.string.phone)+user.getPhoneNo());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.orderDelivered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                order.setDelivered(!order.isDelivered());
                if(holder.orderDelivered.getText().equals(mContext.getString(R.string.mark_delivered))){
                    orderReference.child(order.getOrderId()+order.getCustomerId()).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.orderDelivered.setText(mContext.getString(R.string.mark_undelivered));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            order.setDelivered(!order.isDelivered());
                            Toast.makeText(mContext, mContext.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else{
                    orderReference.child(order.getOrderId()+order.getCustomerId()).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            holder.orderDelivered.setText(mContext.getString(R.string.mark_delivered));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            order.setDelivered(!order.isDelivered());
                            Toast.makeText(mContext, mContext.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, OrderDetailsActivity.class);
                intent.putExtra("userId",order.getCustomerId());
                intent.putExtra("orderId",order.getOrderId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return receivedOrderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView customerImage;
        TextView orderTime,customerName,customerPhoneNo,itemCount,totalPrice;
        Button orderDelivered;
        CardView cardView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderTime = itemView.findViewById(R.id.customer_order_time);
            customerImage = itemView.findViewById(R.id.customer_image);
            customerName = itemView.findViewById(R.id.customer_name);
            customerPhoneNo = itemView.findViewById(R.id.customer_phone_no);
            itemCount = itemView.findViewById(R.id.customer_order_quantity);
            totalPrice = itemView.findViewById(R.id.customer_total_price);
            orderDelivered = itemView.findViewById(R.id.delivered_button);
            cardView = itemView.findViewById(R.id.admin_order_history_card_view);
        }
    }


    private String convertDate(String dateInMilliseconds) {
        if(dateInMilliseconds==null){
            return mContext.getString(R.string.no_record_found);
        }
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", Long.parseLong(dateInMilliseconds)).toString();
    }
}
