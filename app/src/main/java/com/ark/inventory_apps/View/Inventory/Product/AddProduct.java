package com.ark.inventory_apps.View.Inventory.Product;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterAddProduct;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.Model.ModelProduct;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityAddProductBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddProduct extends AppCompatActivity {

    // init binding activity add product
    private ActivityAddProductBinding binding;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;

    // init adapter variant product
    private AdapterAddProduct adapterAddProduct;
    private List<ModelVariants> listVariants;

    // init image launcher
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    // init ref database
    private final DatabaseReference referenceCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("category");
    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");
    private final DatabaseReference referenceProduct = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product");
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");

    // init attr product
    private String keyProduct;
    private String categorySelectKey;
    private String subCategorySelectKey;
    private String nameProduct;
    private String price;
    private String unit;
    private String stock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        // set layout recycler variant product
        binding.recyclerVariantProduct.setLayoutManager(layoutManager);
        binding.recyclerVariantProduct.setItemAnimator(new DefaultItemAnimator());

        // set adapter variant product
        adapterAddProduct = new AdapterAddProduct(this);
        binding.recyclerVariantProduct.setAdapter(adapterAddProduct);

        listVariants = new ArrayList<>();

    }

    private void listenerComponent() {
        pickImageSetup();
        setCategoryProduct();

        // set adapter unit auto complete
        String[] unitChoice = {"Pcs", "Qty", "Box"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item, unitChoice);
        binding.autoCompleteUnit.setAdapter(unitAdapter);

        binding.backBtn.setOnClickListener(view -> finish());

        binding.addVariantProduct.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            setAddVariantBottomDialog();
            bottomSheetDialog.show();
        });

        binding.productImage.setOnClickListener(view -> imagePick.launch("image/*"));

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

        binding.saveProductBtn.setOnClickListener(view -> {
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
            }else if (categorySelectKey.isEmpty()){
                Toast.makeText(this, "Category is not selected", Toast.LENGTH_SHORT).show();
            }else {
                if (onImageChange){

                    if (subCategorySelectKey == null){
                        subCategorySelectKey = "-";
                    }

                    binding.progressCircular.setVisibility(View.VISIBLE);
                    binding.saveProductBtn.setEnabled(false);

                    saveImageProduct();
                }else {
                    Toast.makeText(this, "Image is empty", Toast.LENGTH_SHORT).show();
                }

            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerVariantProduct);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            listVariants.remove(pos);
            adapterAddProduct.notifyItemRemoved(pos);
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // set swipe flags horizontal
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        }
    };

    private void setAddVariantBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_add_variant, null, false);
        TextInputEditText nameVariantTi = viewBottomDialog.findViewById(R.id.name_variant_add);
        Button addBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        addBtn.setOnClickListener(view -> {
            String nameVariant = Objects.requireNonNull(nameVariantTi.getText()).toString();

            if (nameVariant.isEmpty()){
                Toast.makeText(this, "Variant name is empty", Toast.LENGTH_SHORT).show();
            }else {
                // add card to recyclerview adapter
                ModelVariants modelVariants = new ModelVariants(nameVariant);
                listVariants.add(modelVariants);
                adapterAddProduct.setItem(listVariants);
                adapterAddProduct.notifyDataSetChanged();
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
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
                        Toast.makeText(AddProduct.this, "Not yet category", Toast.LENGTH_SHORT).show();
                    }else {
                        binding.autoCompleteCategory.setAdapter(categoryAdapter);
                    }
                },200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageProduct(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("product/"+fileName);

        Bitmap bitmap = ((BitmapDrawable) binding.productImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveProduct(String.valueOf(uri)))
                        .addOnFailureListener(e -> {
                            binding.progressCircular.setVisibility(View.GONE);
                            binding.saveProductBtn.setEnabled(true);
                            Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }));
    }

    private void saveProduct(String url) {
        // set key product
        keyProduct = referenceProduct.push().getKey();

        // get date input
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        String month_name = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
        assert month_name != null;
        String day = month_name.substring(0, 3)+" "+cal.get(Calendar.DATE)+", "+cal.get(Calendar.YEAR);

        ModelProduct modelProduct = new ModelProduct(
                categorySelectKey,
                subCategorySelectKey,
                nameProduct,
                price,
                unit,
                Integer.parseInt(stock),
                day,
                url
        );

        referenceProduct.child(keyProduct).setValue(modelProduct)
                .addOnSuccessListener(unused -> {
                    if (listVariants.size() != 0){
                        saveVariantProduct(keyProduct);
                    }else {
                        binding.progressCircular.setVisibility(View.GONE);
                        binding.saveProductBtn.setEnabled(true);
                        Toast.makeText(this, "Success create product", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressCircular.setVisibility(View.GONE);
                    binding.saveProductBtn.setEnabled(true);
                    Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveVariantProduct(String keyProduct){
        // get size variants product
        int size = listVariants.size();

        for (int i=0; i < size; i++){
            ModelVariants modelVariants = listVariants.get(i);
            referenceVariant.child(keyProduct).push().setValue(modelVariants)
                    .addOnFailureListener(e -> Toast.makeText(AddProduct.this, e.getMessage(), Toast.LENGTH_SHORT).show());

            if (i + 1 == size){
                binding.progressCircular.setVisibility(View.GONE);
                binding.saveProductBtn.setEnabled(true);
                finish();

            }
        }
    }

}