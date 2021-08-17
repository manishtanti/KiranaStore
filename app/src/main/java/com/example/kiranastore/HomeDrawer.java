package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Adapter.CategoryAdapter;
import com.example.kiranastore.Fragment.ProductFragment;
import com.example.kiranastore.Model.Category;
import com.example.kiranastore.Model.User;
import com.example.kiranastore.Singleton.CurrentUserSingleton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;

public class HomeDrawer extends AppCompatActivity {
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private TextView name,email;

    private RecyclerView categoryRecyclerview;
    private CategoryAdapter categoryAdapter;
    private ArrayList<Category> categories;

    private User user;

    private static String language="en";


    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_drawer);


        loadLocale();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_menu);
        profileImage = navigationView.getHeaderView(0).findViewById(R.id.image_profile);
        name = navigationView.getHeaderView(0).findViewById(R.id.name_profile);
        email = navigationView.getHeaderView(0).findViewById(R.id.email_profile);
        categoryRecyclerview = findViewById(R.id.category_recview);
        categoryRecyclerview.setHasFixedSize(true);
        categoryRecyclerview.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        categories = new ArrayList<>();

        getCategories();
        categoryAdapter = new CategoryAdapter(categories,this);
        categoryRecyclerview.setAdapter(categoryAdapter);
        user = CurrentUserSingleton.getInstance().getUser();
        if(user.isShopkeeper()){
            navigationView.getMenu().setGroupEnabled(R.id.admin,true);
        } else{
            navigationView.getMenu().setGroupVisible(R.id.admin,false);
        }
        Picasso.get().load(user.getImageUrl()).placeholder(R.drawable.ic_person).into(profileImage);
        name.setText(user.getFullName());
        email.setText(user.getEmail());


        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.getHeaderView(0).findViewById(R.id.edit_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeDrawer.this, EditProfileActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        refresh();
                        break;

                    case R.id.nav_order_history:
                        if(user.isShopkeeper()){
                            startActivity(new Intent(HomeDrawer.this, AdminOrderHistoryActivity.class));
                        } else {
                            startActivity(new Intent(HomeDrawer.this, UserOrderHistoryActivity.class));
                        }
                        break;

                    case R.id.nav_language:
                        if(language.equals("en")){
                            setLocale("hi");
                        } else{
                            setLocale("en");
                        }
                        refresh();
                        break;

                    case R.id.nav_add_category:
                        startActivity(new Intent(HomeDrawer.this,AddEditCategoryActivity.class));
                        break;

                    case R.id.nav_add_product:
                        intent = new Intent(HomeDrawer.this,AddEditProductActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav_logout:
                        FirebaseAuth.getInstance().signOut();
                        intent = new Intent(HomeDrawer.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }


    private void refresh() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    private void getCategories() {
        FirebaseDatabase.getInstance().getReference("Categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categories.clear();
                for(DataSnapshot val:snapshot.getChildren()){
                    Category cat = val.getValue(Category.class);
                    categories.add(cat);
                }
                if(categories.size()>0){
                    Fragment fragment = new ProductFragment();
                    Bundle args = new Bundle();
                    args.putString("category",categories.get(0).getName());
                    fragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fcontainer,fragment, categories.get(0).getName()).commit();
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        MenuItem item = menu.findItem(R.id.nav_cart);
        MenuItemCompat.setActionView(item, R.layout.actionbar_badge_layout);
        RelativeLayout notificationCount = (RelativeLayout)   MenuItemCompat.getActionView(item);

        ImageView cartIcon = (ImageView) notificationCount.findViewById(R.id.cart_notification_icon);

        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeDrawer.this, CartActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        });

        TextView tv = (TextView) notificationCount.findViewById(R.id.cart_notification_textview);
        FirebaseDatabase.getInstance().getReference("Carts").child(CurrentUserSingleton.getInstance().getUser().getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            tv.setVisibility(View.VISIBLE);
                            tv.setText(snapshot.getChildrenCount()+"");
                        }else{
                            tv.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeDrawer.this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_cart:
                startActivity(new Intent(HomeDrawer.this, CartActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
                break;
        }
        return true;
    }


    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
        SharedPreferences.Editor editor = getSharedPreferences("Settings",MODE_PRIVATE).edit();
        editor.putString("language",lang);
        editor.commit();
    }

    public void loadLocale(){
        SharedPreferences pref = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String lang = pref.getString("language","en");
        language = lang;
        setLocale(lang);
    }


}