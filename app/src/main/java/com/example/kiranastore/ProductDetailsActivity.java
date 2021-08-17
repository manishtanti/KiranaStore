package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.kiranastore.Model.Product;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProductDetailsActivity extends AppCompatActivity {
    private Product product;
    private ImageView image,close;
    private TextView name,desc,price;
    private ElegantNumberButton countButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        product = (Product) getIntent().getSerializableExtra("product");
        image = findViewById(R.id.product_detail_image);
        image.setScaleType(ImageView.ScaleType.FIT_XY);
        name = findViewById(R.id.product_detail_name);
        desc = findViewById(R.id.product_detail_desc);
        price = findViewById(R.id.product_detail_price);
        countButton = findViewById(R.id.product_count);
        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FirebaseDatabase.getInstance().getReference("Carts").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(product.getId())
                .child("quantity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    countButton.setNumber(snapshot.getValue(Integer.class)+"");
                }else{
                    countButton.setNumber("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(product.getImageUrl().equals("default")){
            image.setImageResource(R.drawable.ic_person);
        } else {
            Picasso.get().load(product.getImageUrl()).into(image);
        }
        name.setText(product.getName());
        desc.setText(product.getDescription());
        price.setText("Rs." +product.getPrice());

        countButton.setOnClickListener(new ElegantNumberButton.OnClickListener(){
            @Override
            public void onClick(View view) {
                countButton.setEnabled(false);
                int count = Integer.parseInt(countButton.getNumber());
                if(count==0){
                    FirebaseDatabase.getInstance().getReference("Carts").child(CurrentUserSingleton.getInstance().getUser().getId())
                            .child(product.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductDetailsActivity.this, getString(R.string.product_removed_from_cart), Toast.LENGTH_SHORT).show();
                            countButton.setEnabled(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            countButton.setEnabled(true);
                        }
                    });
                } else {
                    product.setQuantity(count);
                    FirebaseDatabase.getInstance().getReference("Carts").child(CurrentUserSingleton.getInstance().getUser().getId())
                            .child(product.getId()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductDetailsActivity.this, getString(R.string.quantity) + count, Toast.LENGTH_SHORT).show();
                            countButton.setEnabled(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            countButton.setEnabled(true);
                        }
                    });
                }
            }
        });
    }
}