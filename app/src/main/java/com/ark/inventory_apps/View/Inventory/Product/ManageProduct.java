package com.ark.inventory_apps.View.Inventory.Product;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterManageProduct;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.Model.ModelProduct;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityManageProductBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageProduct extends AppCompatActivity {

    // init binding manage product
    private ActivityManageProductBinding binding;

    // init ref database
    private final DatabaseReference referenceProduct = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product");
    private final DatabaseReference referenceCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("category");
    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");

    // init adapter manage product
    private AdapterManageProduct adapterManageProduct;
    private List<ModelProduct> listProduct;

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;

    // init filter data
    private String categorySelectKeyFilter;
    private String subCategorySelectKeyFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageProduct.setLayoutManager(layoutManager);
        binding.recyclerManageProduct.setItemAnimator(new DefaultItemAnimator());

        adapterManageProduct = new AdapterManageProduct(this, binding.progressCircular);
        binding.recyclerManageProduct.setAdapter(adapterManageProduct);

        listProduct = new ArrayList<>();

        referenceProduct.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataProduct();
            }else {
                Toast.makeText(ManageProduct.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.floatAddProduct.setOnClickListener(view -> Functions.updateUI(this, AddProduct.class));

        // set custom filter data product
        bottomSheetDialog = new BottomSheetDialog(this);
        setFilterBottomDialog();
        binding.filterBtn.setOnClickListener(view -> bottomSheetDialog.show());

        // listen user scroll recyclerview
        binding.recyclerManageProduct.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list category
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerManageProduct.getLayoutManager();
                assert linearLayoutManager != null;
                int totalProduct = linearLayoutManager.getItemCount();
                Log.d("Total Product", String.valueOf(totalProduct));
                // check scroll on bottom
                if (!binding.recyclerManageProduct.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalProduct < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataProduct();
                        }
                    }
                }
            }
        });
    }

    private void setFilterBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_filter_manage_product, null, false);

        // layout auto complete
        TextInputLayout layoutSubCategory;
        layoutSubCategory = viewBottomDialog.findViewById(R.id.sub_category_product_filter);

        // auto complete text
        AutoCompleteTextView autoCompleteCategory, autoCompleteSubCategory;
        autoCompleteCategory = viewBottomDialog.findViewById(R.id.auto_complete_category_filter);
        autoCompleteSubCategory = viewBottomDialog.findViewById(R.id.auto_complete_sub_category_filter);

        // text warning
        TextView textWarning = viewBottomDialog.findViewById(R.id.text_warning_sub_category);

        // Button
        Button filterBtn, resetFilterBtn;
        filterBtn = viewBottomDialog.findViewById(R.id.filter_btn);
        resetFilterBtn = viewBottomDialog.findViewById(R.id.reset_filter_btn);

        // set auto complete
        setFilterCategoryProduct(autoCompleteCategory);

        // listen component filter dialog
        autoCompleteCategory.setOnItemClickListener((adapterView, view, i, l) -> {
            ModelCategory modelCategory = (ModelCategory) adapterView.getItemAtPosition(i);
            categorySelectKeyFilter = modelCategory.getKeyCategory();
            setFilterSubCategoryProduct(autoCompleteSubCategory, layoutSubCategory, textWarning);
        });

        autoCompleteSubCategory.setOnItemClickListener((adapterView, view, i, l) -> {
            ModelSubCategory modelSubCategory = (ModelSubCategory) adapterView.getItemAtPosition(i);
            subCategorySelectKeyFilter = modelSubCategory.getKeySubCategory();
        });

        filterBtn.setOnClickListener(view -> {
            key = null;
            listProduct = new ArrayList<>();
            adapterManageProduct.notifyDataSetChanged();
            isLoadData = true;
            setDataProduct();
            bottomSheetDialog.dismiss();
        });

        resetFilterBtn.setOnClickListener(view -> {
            key = null;
            categorySelectKeyFilter = null;
            subCategorySelectKeyFilter = null;

            autoCompleteCategory.setText("");
            autoCompleteSubCategory.setText("");

            listProduct = new ArrayList<>();
            adapterManageProduct.notifyDataSetChanged();
            isLoadData = true;
            setDataProduct();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void setFilterCategoryProduct(AutoCompleteTextView autoCompleteCategory){
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
                        Toast.makeText(ManageProduct.this, "Not yet category", Toast.LENGTH_SHORT).show();
                    }else {
                        autoCompleteCategory.setAdapter(categoryAdapter);
                    }
                },200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setFilterSubCategoryProduct(AutoCompleteTextView autoCompleteSubCategory, TextInputLayout layoutSubCategory, TextView textWarning){
        ArrayAdapter<ModelSubCategory> subCategoryAdapter = new ArrayAdapter<>(this, R.layout.layout_option_item);
        referenceSubCategory.child(categorySelectKeyFilter).addListenerForSingleValueEvent(new ValueEventListener() {
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
                        textWarning.setVisibility(View.VISIBLE);
                        layoutSubCategory.setVisibility(View.GONE);
                    }else {
                        autoCompleteSubCategory.setAdapter(subCategoryAdapter);
                        textWarning.setVisibility(View.GONE);
                        layoutSubCategory.setVisibility(View.VISIBLE);

                    }

                },100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataProduct(){
        binding.progressCircular.setVisibility(View.VISIBLE);
        if (!isLoadData){
            return;
        }

        Query query;

        // set query product data
        // check if user filtering data
        // filter data only one attr
        if (categorySelectKeyFilter != null){
            // check user select sub category filter
            if (subCategorySelectKeyFilter != null){
                // with filter sub category
                if (key == null){
                    query = referenceProduct.orderByChild("keySubCategory").equalTo(subCategorySelectKeyFilter).limitToFirst(10);
                }else {
                    query = referenceProduct.orderByChild("keySubCategory").equalTo(subCategorySelectKeyFilter).startAfter(key).limitToFirst(10);
                }
            }else {
                // with filter category
                if (key == null){
                    query = referenceProduct.orderByChild("keyCategory").equalTo(categorySelectKeyFilter).limitToFirst(10);
                }else {
                    query = referenceProduct.orderByChild("keyCategory").equalTo(categorySelectKeyFilter).startAfter(key).limitToFirst(10);
                }
            }
        }else {
            // no filter (all data show limit 10)
            if (key == null){
                query = referenceProduct.orderByKey().limitToFirst(10);
            }else {
                query = referenceProduct.orderByKey().startAfter(key).limitToFirst(10);
            }
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                    if (modelProduct != null){
                        modelProduct.setKeyProduct(ds.getKey());
                        key = ds.getKey();
                        listProduct.add(modelProduct);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterManageProduct.setItem(listProduct);
                    if (listProduct.size() != 0){
                        adapterManageProduct.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(ManageProduct.this, "Product is empty", Toast.LENGTH_SHORT).show();
                    }
                    binding.progressCircular.setVisibility(View.GONE);
                }, 100);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}