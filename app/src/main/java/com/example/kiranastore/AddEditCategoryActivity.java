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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class AddEditCategoryActivity extends AppCompatActivity {

    private ImageView close;
    CircleImageView categoryImage;
    private TextView save,changePhoto;
    private MaterialEditText categoryName;
    private Button deleteCategory;

    private Uri mImageUri = null;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;

    private Category category;
    private String pastCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);
        category = (Category) getIntent().getSerializableExtra("category");

        close = findViewById(R.id.close);
        save = findViewById(R.id.save);
        categoryImage = findViewById(R.id.edit_category_image);
        changePhoto = findViewById(R.id.change_photo);
        categoryName = findViewById(R.id.edit_category_name);
        deleteCategory = findViewById(R.id.delete_category);

        if(category==null){
            category = new Category();
            pastCategory=null;
        } else{
            setCategory();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference().child("Categories");


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setCropShape(CropImageView.CropShape.OVAL).start(AddEditCategoryActivity.this);
            }
        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(categoryName.getText().toString().trim())){
                    Toast.makeText(AddEditCategoryActivity.this, getString(R.string.name_error), Toast.LENGTH_SHORT).show();
                } else {
                    category.setName(categoryName.getText().toString().trim().toLowerCase());
                    uploadImage();
                }
            }
        });

        deleteCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference.child("Categories").child(pastCategory).removeValue();
                databaseReference.child("Products").child(pastCategory).removeValue();
                finish();
            }
        });
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            mImageUri = result.getUri();
            categoryImage.setImageURI(mImageUri);
            saveBitmapToFile(new File(mImageUri.getPath()));
        } else{
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
        }
    }



    private void uploadImage() {
        if(mImageUri != null){
            ProgressDialog pd = new ProgressDialog(this);
            pd.setTitle(getString(R.string.uploading));
            pd.show();
            StorageReference fileRef = storageRef.child(category.getName());
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
                        category.setImageUrl(downloadUri.toString());
                        updateCategory();
                        pd.dismiss();
                    } else{
                        Toast.makeText(AddEditCategoryActivity.this, getString(R.string.image_upload_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.updating_without_image), Toast.LENGTH_SHORT).show();
            updateCategory();
        }
    }

    private void updateCategory(){

        databaseReference.child("Categories").child(category.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Toast.makeText(AddEditCategoryActivity.this, "Category already exists, Please choose another name", Toast.LENGTH_LONG).show();
                    return;
                }
                else{
                    databaseReference.child("Categories").child(category.getName()).setValue(category);
                    if(pastCategory!=null){
                        appendProductToNewCategory();
                    }
                    Toast.makeText(AddEditCategoryActivity.this, getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show(); 
                    AddEditCategoryActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void appendProductToNewCategory() {
        databaseReference.child("Categories").child(pastCategory).removeValue();     //removed past category
        DatabaseReference ref = databaseReference.child("Products").child(category.getName());  //new category reference

        databaseReference.child("Products").child(pastCategory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot val : snapshot.getChildren()){
                    Product product = val.getValue(Product.class);
                    product.setCategory(category.getName());
                    ref.child(product.getId()).setValue(product);
                }
                    databaseReference.child("Products").child(pastCategory).removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void setCategory(){
        if(category.getImageUrl().equals("default")){
            categoryImage.setImageResource(R.drawable.ic_person);
        }else {
            Picasso.get().load(category.getImageUrl()).into(categoryImage);
        }
        categoryName.setText(category.getName());
        pastCategory = category.getName();
        deleteCategory.setVisibility(View.VISIBLE);
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