package com.example.kiranastore.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiranastore.Model.Product;
import com.example.kiranastore.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OrderDetailsAdapter extends RecyclerView.Adapter<OrderDetailsAdapter.ViewHolder>{
    ArrayList<Product> productList;
    Context mContext;

    public OrderDetailsAdapter(ArrayList<Product> productList, Context mContext) {
        this.productList = productList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.order_details_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        if(product.getImageUrl().equals("default")){
            holder.productImage.setImageResource(R.drawable.ic_person);
        } else{
            Picasso.get().load(product.getImageUrl()).into(holder.productImage);
        }
        holder.productImage.setScaleType(ImageView.ScaleType.FIT_XY);
        holder.productName.setText(product.getName());
        holder.productDesc.setText(product.getDescription());
        holder.productPrice.setText("Rs."+product.getPrice());
        holder.productCount.setText(mContext.getString(R.string.quantity)+ product.getQuantity());
        holder.productTotal.setText(mContext.getString(R.string.total_rs)+product.getPrice()*product.getQuantity());

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName,productDesc,productCount,productPrice,productTotal;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.detail_product_image);
            productName = itemView.findViewById(R.id.detail_product_name);
            productDesc = itemView.findViewById(R.id.detail_product_desc);
            productCount = itemView.findViewById(R.id.detail_product_count);
            productPrice = itemView.findViewById(R.id.detail_per_item_price);
            productTotal = itemView.findViewById(R.id.detail_total_item_price);
        }
    }
}
