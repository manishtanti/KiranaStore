package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Adapter.UserOrderHistoryAdapter;
import com.example.kiranastore.Model.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class UserOrderHistoryActivity extends AppCompatActivity {
    private RecyclerView orderHistoryRecyclerView;
    private UserOrderHistoryAdapter userOrderHistoryAdapter;

    private ImageView close;
    private TextView notFound;

    private ArrayList<String> itemList;
    private ArrayList<String> orderTime;
    private FirebaseUser firebaseUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_history);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        notFound = findViewById(R.id.not_found);
        orderHistoryRecyclerView = findViewById(R.id.order_history_recview);
        orderHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryRecyclerView.setHasFixedSize(true);
        itemList = new ArrayList<>();
        orderTime=new ArrayList<>();
        getOrderHistory();
        userOrderHistoryAdapter = new UserOrderHistoryAdapter(itemList,orderTime,this);
        orderHistoryRecyclerView.setAdapter(userOrderHistoryAdapter);

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void getOrderHistory() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ReceivedOrders");
        FirebaseDatabase.getInstance().getReference("Orders").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot val:snapshot.getChildren()){
                            orderTime.add(val.getKey());
                            int allTotal = 0;
                            StringBuilder details = new StringBuilder("");

                            for(DataSnapshot items:val.getChildren()){
                                Product product = items.getValue(Product.class);
                                int singleTotal=product.getPrice()*product.getQuantity();
                                details.append(product.getName()).append(" : ")
                                        .append(product.getPrice()).append(" x ").append(product.getQuantity()).append(" = ₹ ").append(singleTotal).append("\n");
                                allTotal+=singleTotal;
                            }
                            details.append("\n"+ getString(R.string.total_rs) + allTotal);
                            itemList.add(details.toString());
                        }
                        if(itemList.size()==0){
                            notFound.setVisibility(View.VISIBLE);
                        } else{
                            notFound.setVisibility(View.GONE);
                        }
                        Collections.reverse(orderTime);
                        Collections.reverse(itemList);
                        userOrderHistoryAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}