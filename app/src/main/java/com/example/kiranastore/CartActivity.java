package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Adapter.CartAdapter;
import com.example.kiranastore.Model.Order;
import com.example.kiranastore.Model.Product;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private RecyclerView cartRecyclerview;
    private CartAdapter cartAdapter;
    private ArrayList<Product> cartItems;
    private TextView cartTotal;
    private Button cartButton;
    private ImageView close;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        cartRecyclerview = findViewById(R.id.cart_recview);
        cartTotal = findViewById(R.id.cart_total_value);

        cartButton = findViewById(R.id.cart_button);
        cartRecyclerview.setHasFixedSize(true);
        cartRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        cartItems = new ArrayList<>();
        getCartItems();
        cartAdapter = new CartAdapter(cartItems,this);
        cartRecyclerview.setAdapter(cartAdapter);

        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cartButton.getText().equals(getString(R.string.go_to_home))){
                    finish();
                }
                else if(CurrentUserSingleton.getInstance().getUser().isShopkeeper()){
                    Toast.makeText(CartActivity.this, getString(R.string.you_cannot_order_to_yourself), Toast.LENGTH_SHORT).show();
                }
                else if(cartButton.getText().equals(getString(R.string.place_order))){
                    String orderId = System.currentTimeMillis()+"";


                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Orders").child(firebaseUser.getUid()).child(orderId);
                    FirebaseDatabase.getInstance().getReference("Carts").child(firebaseUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int items=0,total=0;
                                    for(DataSnapshot val : snapshot.getChildren()){
                                        Product product = val.getValue(Product.class);
                                        ref.child(product.getId()).setValue(product);
                                        ++items;
                                        total+=product.getPrice()*product.getQuantity();
                                    }
                                    sendOrderToShopkeeper(orderId,items,total);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error){
                                    Toast.makeText(CartActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                } else {
                    finish();
                }
            }
        });
    }

    private void sendOrderToShopkeeper(String orderId, int items, int total) {
        Order order = new Order(firebaseUser.getUid(),orderId,items,total,false);
        FirebaseDatabase.getInstance().getReference("ReceivedOrders").child(orderId+firebaseUser.getUid()).setValue(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseDatabase.getInstance().getReference("Carts").child(firebaseUser.getUid()).removeValue();
                        Toast.makeText(CartActivity.this, getString(R.string.order_successful), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CartActivity.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCartItems() {

        FirebaseDatabase.getInstance().getReference("Carts").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total=0;
                cartItems.clear();
                for(DataSnapshot val : snapshot.getChildren()){
                    Product product = val.getValue(Product.class);
                    total += product.getPrice()*product.getQuantity();
                    cartItems.add(product);
                }
                if(cartItems.isEmpty()){
                    cartTotal.setText(getString(R.string.cart_empty));
                    cartButton.setText(getString(R.string.go_to_home));
                } else {
                    cartTotal.setText(getString(R.string.total_rs) + total);
                    cartButton.setText(getString(R.string.place_order));
                }
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}