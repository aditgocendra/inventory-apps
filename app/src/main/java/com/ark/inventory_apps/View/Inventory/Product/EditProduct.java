package com.ark.inventory_apps.View.Inventory.Product;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.Model.ModelProduct;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityEditProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class EditProduct extends AppCompatActivity {

    // init binding activity product edit
    private ActivityEditProductBinding binding;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    // init ref database
    private final DatabaseReference referenceCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("category");
    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");
    private final DatabaseReference referenceProduct = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product");

    // init attr product
    private String keyProduct;
    private String oldImageUrl;
    private String categorySelectKey;
    private String subCategorySelectKey;
    private String nameProduct;
    private String price;
    private String unit;
    private String stock;
    private String dateInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyProduct = getIntent().getStringExtra("keyProduct");
        oldImageUrl = getIntent().getStringExtra("oldImageProduct");

        listenerComponent();

        setDataProduct();
    }

    private void listenerComponent() {
        pickImageSetup();
        setCategoryProduct();

        binding.backBtn.setOnClickListener(view -> finish());

        // set adapter unit auto complete
        String[] unitChoice = {"Pcs", "Qty", "Box"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item, unitChoice);
        binding.autoCompleteUnit.setAdapter(unitAdapter);

        binding.autoCompleteCategory.setOnItemClickListener((adapterView, view, i, l) -> {
            ModelCategory modelCategory = (ModelCategory) adapterView.getItemAtPosition(i);
            categorySelectKey = modelCategory.getKeyCategory();
            setSubCategoryProduct();
        });

        binding.autoCompleteSubCategory.setOnItemClickListener((adapterView, view, i, l) -> {
            ModelSubCategory modelSubCategory = (ModelSubCategory) adapterView.getItemAtPosition(i);
            subCategorySelectKey = modelSubCategory.getKeySubCategory();
            setSubCategoryProduct();
        });

        binding.productImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.editProductBtn.setOnClickListener(view -> {
            nameProduct = Objects.requireNonNull(binding.nameProductTi.getText()).toString();
            price = Objects.requireNonNull(binding.priceProductTi.getText()).toString();
            unit = Objects.requireNonNull(binding.autoCompleteUnit.getText()).toString();
            stock = Objects.requireNonNull(binding.stockProductTi.getText()).toString();

            // validate form
            if (nameProduct.isEmpty()){
                Toast.makeText(this, "Product name is empty", Toast.LENGTH_SHORT).show();
            }else if (price.isEmpty()){
                Toast.makeText(this, "Price is empty", Toast.LENGTH_SHORT).show();
            }else if (unit.isEmpty()){
                Toast.makeText(this, "Unit is empty", Toast.LENGTH_SHORT).show();
            }else if (stock.isEmpty()){
                Toast.makeText(this, "Stock is empty", Toast.LENGTH_SHORT).show();
            }else if (categorySelectKey == null){
                Toast.makeText(this, "Category is not selected", Toast.LENGTH_SHORT).show();
            }else {

                if (subCategorySelectKey == null){
                    subCategorySelectKey = "-";
                }

                if (onImageChange){
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    binding.editProductBtn.setEnabled(false);

                    editWithImageProduct();
                }else {
                    editProduct(oldImageUrl);
                }

            }
        });
    }

    private void pickImageSetup() {
        imagePick = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    binding.productImage.setImageURI(result);
                    onImageChange = true;
                }
        );
    }

    private void setCategoryProduct(){
        ArrayAdapter<ModelCategory> categoryAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item);
        referenceCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    if (modelCategory != null){
                        modelCategory.setKeyCategory(ds.getKey());
                        categoryAdapter.add(modelCategory);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (categoryAdapter.isEmpty()){
                        Toast.makeText(EditProduct.this, "Not yet category", Toast.LENGTH_SHORT).show();
                    }else {
                        binding.autoCompleteCategory.setAdapter(categoryAdapter);
                    }
                },200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSubCategoryProduct(){
        ArrayAdapter<ModelSubCategory> subCategoryAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item);
        referenceSubCategory.child(categorySelectKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelSubCategory modelSubCategory = ds.getValue(ModelSubCategory.class);
                    if (modelSubCategory != null){
                        modelSubCategory.setKeySubCategory(ds.getKey());
                        subCategoryAdapter.add(modelSubCategory);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (subCategoryAdapter.isEmpty()){
                        binding.textWarningSubCategory.setVisibility(View.VISIBLE);
                        binding.subCategoryProduct.setVisibility(View.GONE);
                    }else {
                        binding.autoCompleteSubCategory.setAdapter(subCategoryAdapter);
                        binding.textWarningSubCategory.setVisibility(View.GONE);
                        binding.subCategoryProduct.setVisibility(View.VISIBLE);

                    }

                },200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataProduct() {
        referenceProduct.child(keyProduct).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProduct modelProduct = task.getResult().getValue(ModelProduct.class);
                if (modelProduct != null){
                    Picasso.get().load(modelProduct.getUrlImage()).into(binding.productImage);
                    binding.nameProductTi.setText(modelProduct.getNameProduct());
                    binding.priceProductTi.setText(modelProduct.getPriceDefault());
                    binding.stockProductTi.setText(String.valueOf(modelProduct.getStock()));

                    binding.autoCompleteUnit.setText(modelProduct.getUnit(), false);
                    binding.autoCompleteUnit.setSelection(binding.autoCompleteUnit.getText().length());

                    dateInput = modelProduct.getDateInput();

                }
            }else {
                Toast.makeText(EditProduct.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editWithImageProduct() {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(oldImageUrl).getName();
        StorageReference deleteRef = referenceStorage.getReference("product/"+name_photo);

        deleteRef.delete().addOnSuccessListener(unused -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = dateFormat.format(now);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("product/"+fileName);

            Bitmap bitmap = ((BitmapDrawable) binding.productImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
            byte[] data = baOs.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask
                    .addOnFailureListener(e -> {
                        binding.progressCircular.setVisibility(View.GONE);
                        binding.editProductBtn.setEnabled(true);
                        Toast.makeText(EditProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> editProduct(String.valueOf(uri)))
                            .addOnFailureListener(e -> {
                                binding.progressCircular.setVisibility(View.GONE);
                                binding.editProductBtn.setEnabled(true);
                                Toast.makeText(EditProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }));

        }).addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.GONE);
            binding.editProductBtn.setEnabled(true);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void editProduct(String url) {
        ModelProduct modelProduct = new ModelProduct(
                categorySelectKey,
                subCategorySelectKey,
                nameProduct,
                price,
                unit,
                Integer.parseInt(stock),
                dateInput,
                url
        );

        referenceProduct.child(keyProduct).setValue(modelProduct).addOnSuccessListener(unused -> {
            Toast.makeText(EditProduct.this, "Success edit product data", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        binding.progressCircular.setVisibility(View.GONE);
        binding.editProductBtn.setEnabled(true);

    }

}