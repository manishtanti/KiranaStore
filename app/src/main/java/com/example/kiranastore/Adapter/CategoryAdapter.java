package com.example.kiranastore.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kiranastore.AddEditCategoryActivity;
import com.example.kiranastore.AddEditProductActivity;
import com.example.kiranastore.Fragment.ProductFragment;
import com.example.kiranastore.Model.Category;
import com.example.kiranastore.Model.User;
import com.example.kiranastore.R;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private ArrayList<Category> categories;
    private Context mContext;
    private User user;

    public CategoryAdapter(ArrayList<Category> categories, Context mContext) {
        this.categories = categories;
        this.mContext = mContext;
        user = CurrentUserSingleton.getInstance().getUser();
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        if(cat.getImageUrl().equals("default")){
            holder.categoryImage.setImageResource(R.drawable.ic_category);
        } else {
            Picasso.get().load(cat.getImageUrl()).into(holder.categoryImage);
        }
        holder.categoryName.setText(cat.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fManager = ((FragmentActivity)mContext).getSupportFragmentManager();
                Fragment fragment = fManager.findFragmentByTag(cat.getName());

                if (fragment == null) {
                    fragment = new ProductFragment();
                    Bundle args = new Bundle();
                    args.putString("category",cat.getName());
                    fragment.setArguments(args);
                    fManager.beginTransaction().add(R.id.fcontainer,fragment, cat.getName()).commit();
                }
                else {
                    fManager.beginTransaction().replace(R.id.fcontainer, fragment, cat.getName()).commit();
                }
            }
        });

        if(user.isShopkeeper()) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Intent intent = new Intent(mContext, AddEditCategoryActivity.class);
                    intent.putExtra("category", cat);
                    mContext.startActivity(intent);
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView categoryImage;
        TextView categoryName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage=itemView.findViewById(R.id.category_image);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }
}
