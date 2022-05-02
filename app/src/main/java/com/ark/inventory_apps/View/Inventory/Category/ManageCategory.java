package com.ark.inventory_apps.View.Inventory.Category;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterManageCategory;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityManageCategoryBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ManageCategory extends AppCompatActivity {

    // init view binding
    private ActivityManageCategoryBinding binding;

    // ref to category user inventory
    private final DatabaseReference referenceCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("category");

    // init adapter
    private AdapterManageCategory adapterManageCategory;
    private final List<ModelCategory> listCategory = new ArrayList<>();

    // param pagination data
    private long countData;
    private String key = null;
    private boolean isLoadData = false;

    // init bottom sheet dialog for CRUD
    private BottomSheetDialog bottomSheetDialog;

    // init bottom sheet dialog for sort dialog
    private BottomSheetDialog bottomSheetDialogSort;

    // init bool sort
    // true == ascending
    // false == descending
    private boolean sort = true;

    // init item touch helper for swipe
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageCategoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerManageCategory.setLayoutManager(layoutManager);
        binding.recyclerManageCategory.setItemAnimator(new DefaultItemAnimator());

        adapterManageCategory = new AdapterManageCategory(this);
        binding.recyclerManageCategory.setAdapter(adapterManageCategory);

        requestDataCategory();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.floatAddCategory.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(ManageCategory.this);
            setAddBottomDialog();
            bottomSheetDialog.show();
        });

        bottomSheetDialogSort = new BottomSheetDialog(ManageCategory.this);
        setSortBottomDialog();
        binding.sortBtn.setOnClickListener(view -> {
            bottomSheetDialogSort.show();
        });

        // listen user scroll recyclerview
        binding.recyclerManageCategory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // get total item in list category
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) binding.recyclerManageCategory.getLayoutManager();
                assert linearLayoutManager != null;
                int totalCategory = linearLayoutManager.getItemCount();
                Log.d("Total Category", String.valueOf(totalCategory));
                // check scroll on bottom
                if (!binding.recyclerManageCategory.canScrollVertically(1) && newState==RecyclerView.SCROLL_STATE_IDLE){
                    // check data item if total item < total data in database == load more data
                    if (totalCategory < countData){
                        // load more data
                        if (!isLoadData){
                            isLoadData = true;
                            setDataCategory();
                        }
                    }
                }
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerManageCategory);
    }
    // helper touch drag
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int pos = viewHolder.getAdapterPosition();
            Collections.swap(listCategory, pos, pos);
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // onSwiped never call, always revert
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            // get position card on adapter
            int posAdapter = viewHolder.getAdapterPosition();

            // get category from list
            ModelCategory modelCategory = listCategory.get(posAdapter);

            // set bottom action dialog from adapter
            adapterManageCategory.setActionBottomDialog(modelCategory, posAdapter);
            adapterManageCategory.bottomSheetDialog.show();

            return 1f;
        }

        @Override
        public float getSwipeEscapeVelocity(float defaultValue) {
            return Float.MAX_VALUE;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            // set swipe flags horizontal
            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(0, swipeFlags);
        }
    };

    private void setAddBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_add_category, null, false);
        TextInputEditText categoryEt = viewBottomDialog.findViewById(R.id.category_add);
        Button addBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        addBtn.setOnClickListener(view -> {
            String category = Objects.requireNonNull(categoryEt.getText()).toString();

            if (category.isEmpty()){
                Toast.makeText(this, "Category name is empty", Toast.LENGTH_SHORT).show();
            }else {
                saveCategory(category);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void setSortBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_filter_category, null, false);
        MaterialButton ascendingBtn = viewBottomDialog.findViewById(R.id.ascending_btn);
        MaterialButton descendingBtn = viewBottomDialog.findViewById(R.id.descending_btn);
        MaterialButton finishBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        ascendingBtn.setTextColor(Color.WHITE);
        ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

        ascendingBtn.setOnClickListener(view -> {

            // set component color
            ascendingBtn.setTextColor(Color.WHITE);
            ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            descendingBtn.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(descendingBtn, ColorStateList.valueOf(Color.TRANSPARENT));

            sort = true;

        });

        descendingBtn.setOnClickListener(view -> {

            // set component color
            descendingBtn.setTextColor(Color.WHITE);
            ViewCompat.setBackgroundTintList(descendingBtn, ColorStateList.valueOf(getResources().getColor(R.color.primary_dark)));

            ascendingBtn.setTextColor(getResources().getColor(R.color.primary_dark));
            ViewCompat.setBackgroundTintList(ascendingBtn, ColorStateList.valueOf(Color.TRANSPARENT));

            sort = false;

        });

        finishBtn.setOnClickListener(view -> {
            sortList();
            adapterManageCategory.notifyDataSetChanged();
            bottomSheetDialogSort.dismiss();
        });

        bottomSheetDialogSort.setContentView(viewBottomDialog);
    }

    private void requestDataCategory(){
        referenceCategory.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                countData = task.getResult().getChildrenCount();
                isLoadData = true;
                setDataCategory();
            }else {
                Toast.makeText(ManageCategory.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataCategory(){
        if (!isLoadData){
            return;
        }

        Query query;
        if (key == null){
            query = referenceCategory.orderByKey().limitToFirst(10);
        }else {
            query = referenceCategory.orderByKey().startAfter(key).limitToFirst(10);
        }

        isLoadData = true;

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelCategory modelCategory = ds.getValue(ModelCategory.class);
                    if (modelCategory != null){
                        modelCategory.setKeyCategory(ds.getKey());
                        key = ds.getKey();
                        listCategory.add(modelCategory);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    adapterManageCategory.setItem(listCategory);
                    if (listCategory.size() != 0){
                        sortList();
                        adapterManageCategory.notifyDataSetChanged();
                        isLoadData = false;
                    }else {
                        Toast.makeText(ManageCategory.this, "Category is empty", Toast.LENGTH_SHORT).show();
                    }
                }, 200);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageCategory.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveCategory(String category){
        ModelCategory modelCategory = new ModelCategory(category);
        String keyCategory = referenceCategory.push().getKey();

        if (keyCategory == null){
            Toast.makeText(this, "Failed insert data to database", Toast.LENGTH_SHORT).show();
            return;
        }

        referenceCategory.child(keyCategory).setValue(modelCategory).addOnSuccessListener(unused -> {
            Toast.makeText(ManageCategory.this, "Success add new category", Toast.LENGTH_SHORT).show();

            // add new data add in list
            modelCategory.setKeyCategory(keyCategory);
            listCategory.add(modelCategory);

            // notify category add in adapter
            adapterManageCategory.notifyDataSetChanged();

            // update last key
            key = keyCategory;

        }).addOnFailureListener(e -> Toast.makeText(ManageCategory.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void sortList(){

        if (sort){
            // ascending
            Collections.sort(listCategory, (modelCategory1, modelCategory2) -> {
                String category1 = modelCategory1.getCategory();
                String category2 = modelCategory2.getCategory();
                return category1.compareToIgnoreCase(category2);
            });
        }else {
            // descending
            Collections.sort(listCategory, (modelCategory1, modelCategory2) -> {
                String category1 = modelCategory1.getCategory();
                String category2 = modelCategory2.getCategory();
                return category2.compareToIgnoreCase(category1);
            });
        }

    }


}