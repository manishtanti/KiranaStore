package com.example.kiranastore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kiranastore.Model.Category;
import com.example.kiranastore.Model.Product;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class AddEditProductActivity extends AppCompatActivity {

    private Spinner spinner;
    private ImageView close,productImage;
    private TextView save,changePhoto;
    private MaterialEditText productName,productDesc,productPrice;
    private Button deleteProduct;

    private Uri mImageUri = null;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;

    private Product product;
    String preCat=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);
        product = (Product)getIntent().getSerializableExtra("product");

        close = findViewById(R.id.close);
        save = findViewById(R.id.save);
        productImage = findViewById(R.id.edit_product_image);
        spinner = findViewById(R.id.category_spinner);
        changePhoto = findViewById(R.id.change_photo);
        productName = findViewById(R.id.edit_product_name);
        productDesc = findViewById(R.id.edit_product_desc);
        productPrice = findViewById(R.id.edit_product_price);
        deleteProduct = findViewById(R.id.delete_product);
        getCategories();

        if(product==null){
            product = new Product();
        } else{
            setProduct();
        }



        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child("Products");


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.RECTANGLE).start(AddEditProductActivity.this);
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(TextUtils.isEmpty(productName.getText().toString().trim()) || TextUtils.isEmpty(productDesc.getText().toString().trim()) || TextUtils.isEmpty(productPrice.getText().toString())){
                    Toast.makeText(AddEditProductActivity.this, getString(R.string.empty_credentials), Toast.LENGTH_SHORT).show();
                }else if(Integer.parseInt(productPrice.getText().toString())<=0) {
                    Toast.makeText(AddEditProductActivity.this, getString(R.string.price_error), Toast.LENGTH_SHORT).show();
                }
                else {
                    product.setName(productName.getText().toString().trim());
                    product.setDescription(productDesc.getText().toString().trim());
                    product.setPrice(Integer.parseInt(productPrice.getText().toString().trim()));
                    product.setCategory(spinner.getSelectedItem().toString());
                    uploadImage();
                }
            }
        });

        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Products").child(product.getCategory()).child(product.getId()).removeValue();
                finish();
            }
        });
    }

    private void getCategories() {
        FirebaseDatabase.getInstance().getReference("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> categories = new ArrayList<>();
                for(DataSnapshot val : snapshot.getChildren()){
                    categories.add(val.getKey());
                }
                if(categories.size()==0){
                    categories.add("Default");
                }
                spinner.setAdapter(new ArrayAdapter<String>(AddEditProductActivity.this, android.R.layout.simple_spinner_dropdown_item,categories));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateProduct(){
        String newCat = spinner.getSelectedItem().toString();
        databaseReference.child("Products").child(newCat).child(product.getId()).setValue(product);
        if(spinner.getSelectedItem().toString().equals("Default")){
            databaseReference.child("Categories").child("Default").setValue(new Category("Default","default"));
        }
        if(preCat!=null && !preCat.equals(newCat)){
            databaseReference.child("Products").child(preCat).child(product.getId()).removeValue();
        }

        Toast.makeText(this, getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            productImage.setImageURI(mImageUri);
            saveBitmapToFile(new File(mImageUri.getPath()));
        } else{
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }



    private void uploadImage() {
        if(product.getId()==null) {
            product.setId(databaseReference.child("Products").push().getKey());
        }

        if(mImageUri != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle(getString(R.string.uploading));
            pd.show();
            StorageReference fileRef = storageRef.child(product.getId());
            uploadTask = fileRef.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        product.setImageUrl(downloadUri.toString());
                        updateProduct();
                        pd.dismiss();
                    } else{
                        Toast.makeText(AddEditProductActivity.this, getString(R.string.image_upload_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.updating_without_image), Toast.LENGTH_SHORT).show();
            updateProduct();
        }
    }
    public void setProduct(){
        productImage.setScaleType(ImageView.ScaleType.FIT_XY);
        if(product.getImageUrl().equals("default")){
            productImage.setImageResource(R.drawable.ic_person);

        }else {
            Picasso.get().load(product.getImageUrl()).into(productImage);
        }
        preCat = product.getCategory();
        productName.setText(product.getName());
        productDesc.setText(product.getDescription());
        productPrice.setText(product.getPrice().toString());
        deleteProduct.setVisibility(View.VISIBLE);
    }
    public File saveBitmapToFile(File file){
        try {

            // BitmapFactory options to downsize the image
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            o.inSampleSize = 6;
            // factor of downsizing the image

            FileInputStream inputStream = new FileInputStream(file);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, o);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE=20;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            inputStream = new FileInputStream(file);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, o2);
            inputStream.close();

            // here i override the original image file
            file.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(file);

            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100 , outputStream);

            return file;
        } catch (Exception e) {
            return null;
        }
    }
}

