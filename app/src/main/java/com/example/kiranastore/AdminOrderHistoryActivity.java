package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Adapter.AdminOrderHistoryAdapter;
import com.example.kiranastore.Model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AdminOrderHistoryActivity extends AppCompatActivity {
    private RecyclerView AOHRecyclerview;
    private AdminOrderHistoryAdapter AOHAdapter;
    private ArrayList<Order> orderList;
    private ImageView close;
    private TextView notFound;
    private Button allOrders,pendingOrders,deliveredOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_history);

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        notFound = findViewById(R.id.not_found);

        allOrders = findViewById(R.id.all_orders);
        pendingOrders = findViewById(R.id.pending_orders);
        deliveredOrders = findViewById(R.id.delivered_orders);

        AOHRecyclerview = findViewById(R.id.admin_order_history_recview);
        AOHRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        AOHRecyclerview.setHasFixedSize(true);
        orderList = new ArrayList<>();
        getOrders(0);
        AOHAdapter = new AdminOrderHistoryAdapter(orderList,this);
        AOHRecyclerview.setAdapter(AOHAdapter);

        allOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrders(0);

            }
        });

        pendingOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrders(1);

            }
        });

        deliveredOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOrders(2);

            }
        });


    }

    private void getOrders(int type) {
        FirebaseDatabase.getInstance().getReference("ReceivedOrders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for(DataSnapshot val:snapshot.getChildren()){
                    Order order = val.getValue(Order.class);
                    if(type==0) {
                        orderList.add(order);
                    } else if(type==1){
                        if(!order.isDelivered()){
                            orderList.add(order);
                        }
                    } else if(type==2){
                        if(order.isDelivered()){
                            orderList.add(order);
                        }
                    }
                }
                if(orderList.size()==0){
                    notFound.setVisibility(View.VISIBLE);
                } else{
                    notFound.setVisibility(View.GONE);
                }
                Collections.reverse(orderList);
                AOHAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminOrderHistoryActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }
}