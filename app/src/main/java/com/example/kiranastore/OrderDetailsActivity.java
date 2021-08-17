package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kiranastore.Adapter.OrderDetailsAdapter;
import com.example.kiranastore.Model.Product;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class OrderDetailsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OrderDetailsAdapter adapter;
    private ArrayList<Product> productList;

    private TextView orderTime,overallTotal;
    private ImageView close;
    private String userId,orderId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        userId = getIntent().getStringExtra("userId");
        orderId = getIntent().getStringExtra("orderId");

        orderTime = findViewById(R.id.order_time);
        overallTotal = findViewById(R.id.overall_total);

        orderTime.setText(convertDate(orderId));
        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.order_details_recview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        productList = new ArrayList<>();
        getProducts();
        adapter = new OrderDetailsAdapter(productList,this);
        recyclerView.setAdapter(adapter);
    }

    private void getProducts() {
        if(userId == null || orderId == null){
            return;
        }
        FirebaseDatabase.getInstance().getReference("Orders").child(userId).child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total=0;
                for(DataSnapshot val : snapshot.getChildren()){
                    Product product = val.getValue(Product.class);
                    productList.add(product);
                    total+=product.getPrice()*product.getQuantity();
                }
                overallTotal.setText(getString(R.string.total_rs)+total);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String convertDate(String dateInMilliseconds) {
        if(dateInMilliseconds==null){
            return getString(R.string.no_record_found);
        }
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", Long.parseLong(dateInMilliseconds)).toString();
    }
}