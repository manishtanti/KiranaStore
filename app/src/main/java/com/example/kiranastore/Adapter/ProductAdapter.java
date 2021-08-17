package com.example.kiranastore.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiranastore.AddEditProductActivity;
import com.example.kiranastore.Model.Product;
import com.example.kiranastore.Model.User;
import com.example.kiranastore.ProductDetailsActivity;
import com.example.kiranastore.R;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>{
    ArrayList<Product> productList;
    Context mContext;
    User user;

    public ProductAdapter(ArrayList<Product> productList, Context mContext) {
        this.productList = productList;
        this.mContext = mContext;
        user = CurrentUserSingleton.getInstance().getUser();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.product_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        Picasso.get().load(product.getImageUrl()).placeholder(R.drawable.ic_person).into(holder.productImage);
        holder.productImage.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.productName.setText(product.getName());
        holder.productDesc.setText(product.getDescription());
        holder.productPrice.setText("₹ " + product.getPrice());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProductDetailsActivity.class);
                intent.putExtra("product",product);
                mContext.startActivity(intent);
            }
        });
        if (user.isShopkeeper()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(mContext, AddEditProductActivity.class);
                    intent.putExtra("product", product);
                    mContext.startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName,productDesc,productPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productDesc = itemView.findViewById(R.id.product_desc);
            productPrice = itemView.findViewById(R.id.product_price);
        }
    }
}
