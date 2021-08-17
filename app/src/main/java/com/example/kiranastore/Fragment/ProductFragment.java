package com.example.kiranastore.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Adapter.ProductAdapter;
import com.example.kiranastore.Model.Product;
import com.example.kiranastore.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ProductFragment extends Fragment {
    private RecyclerView productRecyclerview;
    private ProductAdapter productAdapter;
    private ArrayList<Product> productList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_product, container, false);

        productRecyclerview = view.findViewById(R.id.product_recview);
        productRecyclerview.setLayoutManager(new GridLayoutManager(getContext(),2));
        productRecyclerview.setHasFixedSize(true);
        productList = new ArrayList<>();
        String category = getArguments().getString("category",null);
        getProducts(category);
        productAdapter = new ProductAdapter(productList,getContext());
        productRecyclerview.setAdapter(productAdapter);



        return view;
    }

    private void getProducts(String category) {
        if(category==null){
            return;
        }
        FirebaseDatabase.getInstance().getReference("Products").child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for(DataSnapshot val:snapshot.getChildren()){
                    Product product = val.getValue(Product.class);
                    productList.add(product);
                }
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}