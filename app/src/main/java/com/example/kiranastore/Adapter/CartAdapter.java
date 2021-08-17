package com.example.kiranastore.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import com.example.kiranastore.Model.Product;
import com.example.kiranastore.R;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {
    ArrayList<Product> cartItems;
    private Context mContext;
    AlertDialog alertDialog;

    public CartAdapter(ArrayList<Product> cartItems, Context mContext) {
        this.cartItems = cartItems;
        this.mContext = mContext;
        alertDialog = new AlertDialog.Builder(mContext).create();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.cart_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = cartItems.get(position);
        Picasso.get().load(cartItems.get(position).getImageUrl()).into(holder.image);
        holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.name.setText(product.getName());
        holder.desc.setText(product.getDescription());
        holder.price.setText("₹ "+product.getPrice());
        holder.button.setNumber(""+cartItems.get(position).getQuantity());
        holder.total.setText("₹ " + product.getPrice()*Integer.parseInt(holder.button.getNumber()));

        holder.button.setOnClickListener(new ElegantNumberButton.OnClickListener(){
            @Override
            public void onClick(View view) {
                holder.button.setEnabled(false);
                int count = Integer.parseInt(holder.button.getNumber());
                if(count==0){
                    FirebaseDatabase.getInstance().getReference("Carts").child(CurrentUserSingleton.getInstance().getUser().getId())
                            .child(product.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, mContext.getString(R.string.product_removed_from_cart), Toast.LENGTH_SHORT).show();
                            holder.button.setEnabled(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            holder.button.setEnabled(true);
                        }
                    });
                } else {
                    product.setQuantity(count);
                    FirebaseDatabase.getInstance().getReference("Carts").child(CurrentUserSingleton.getInstance().getUser().getId())
                            .child(product.getId()).setValue(product).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(mContext, mContext.getString(R.string.quantity) + count, Toast.LENGTH_SHORT).show();
                            holder.button.setEnabled(true);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            holder.button.setEnabled(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name,desc,price,total;
        ElegantNumberButton button;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cart_image);
            name = itemView.findViewById(R.id.cart_name);
            desc = itemView.findViewById(R.id.cart_desc);
            price = itemView.findViewById(R.id.cart_price);
            button = itemView.findViewById(R.id.cart_item_count);
            total = itemView.findViewById(R.id.cart_item_total);

        }
    }
}
