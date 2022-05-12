package com.ark.inventory_apps.View.Inventory.Category;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterManageSubCategory;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityManageSubCategoryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageSubCategory extends AppCompatActivity {

    // init binding view manage sub category
    private ActivityManageSubCategoryBinding binding;

    // ref to sub category user inventory
    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");

    // init data intent
    private String keyCategory;

    // init adapter
    private AdapterManageSubCategory adapterManageSubCategory;
    private List<ModelSubCategory> listSubCategory;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageSubCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

        // get intent extra data
        keyCategory = getIntent().getStringExtra("key");
        String category = getIntent().getStringExtra("category");

        // set name category
        binding.textNameCategory.setText(category);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageSubCategory.setLayoutManager(layoutManager);
        binding.recyclerManageSubCategory.setItemAnimator(new DefaultItemAnimator());
        
        setDataSubCategory();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.addSubCategory.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(ManageSubCategory.this);
            setAddBottomDialog();
            bottomSheetDialog.show();
        });
    }

    private void setAddBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_add_sub_category, null, false);
        TextInputEditText subCategoryEt = viewBottomDialog.findViewById(R.id.sub_category_add);
        Button addBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        addBtn.setOnClickListener(view -> {
            String subCategory = Objects.requireNonNull(subCategoryEt.getText()).toString();

            if (subCategory.isEmpty()){
                Toast.makeText(this, "Category name is empty", Toast.LENGTH_SHORT).show();
            }else {
                saveSubCategory(subCategory);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void saveSubCategory(String subCategory) {
        ModelSubCategory modelSubCategory = new ModelSubCategory(subCategory);

        referenceSubCategory.child(keyCategory).push().setValue(modelSubCategory)
                .addOnSuccessListener(unused -> Toast.makeText(ManageSubCategory.this, "Success add sub category", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ManageSubCategory.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setDataSubCategory() {
        referenceSubCategory.child(keyCategory).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listSubCategory = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelSubCategory modelSubCategory = ds.getValue(ModelSubCategory.class);
                    if (modelSubCategory != null){
                        modelSubCategory.setKeySubCategory(ds.getKey());
                        listSubCategory.add(modelSubCategory);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listSubCategory.size() != 0){
                        adapterManageSubCategory = new AdapterManageSubCategory(ManageSubCategory.this, listSubCategory, keyCategory);
                        binding.recyclerManageSubCategory.setAdapter(adapterManageSubCategory);
                    }else {
                        Toast.makeText(ManageSubCategory.this, "There are no sub-categories in this category", Toast.LENGTH_SHORT).show();
                    }
                },200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageSubCategory.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}