package com.ark.inventory_apps.View.Inventory.Product;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterManageVariant;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityManageVariantBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ManageVariant extends AppCompatActivity {

    // init activity binding
    private ActivityManageVariantBinding binding;

    // init ref root variant
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");
    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");

    // init adapter variant product
    private AdapterManageVariant adapterManageVariant;
    private List<ModelVariants> listVariant;

    // init local variable
    private String keyProduct;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;

    // init storage firebase
    FirebaseStorage referenceStorage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyProduct = getIntent().getStringExtra("keyProduct");

        listenerComponent();

        // layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        // set layout recycler variant product
        binding.recyclerManageVariant.setLayoutManager(layoutManager);
        binding.recyclerManageVariant.setItemAnimator(new DefaultItemAnimator());

        // set adapter recyclerview
        adapterManageVariant = new AdapterManageVariant(ManageVariant.this, keyProduct);
        binding.recyclerManageVariant.setAdapter(adapterManageVariant);

        setDataVariantProduct();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.floatAddVariant.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(this);
            setAddVariantBottomDialog();
            bottomSheetDialog.show();
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(binding.recyclerManageVariant);
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.START | ItemTouchHelper.END, 0) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            //Create the Dialog here
            Dialog dialog = new Dialog(ManageVariant.this);
            dialog.setContentView(R.layout.layout_confirmation_delete);
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_center_dialog));

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            dialog.setCancelable(false); //Optional
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

            Button Okay = dialog.findViewById(R.id.btn_okay);
            Button Cancel = dialog.findViewById(R.id.btn_cancel);

            TextView textBody = dialog.findViewById(R.id.textView2);

            int totalVariant = listVariant.size();

            if (totalVariant < 2){
                textBody.setText("Are you sure to delete all data variants? \n All product data from this variant will be deleted");
            }

            dialog.show();

            Okay.setOnClickListener(v -> {
                // new data category
                deleteVariant(pos);
                dialog.dismiss();
            });

            Cancel.setOnClickListener(v -> {
                adapterManageVariant.notifyItemChanged(pos);
                dialog.dismiss();
            });
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
                // add new variant
                addVariant(nameVariant);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void setDataVariantProduct() {
        listVariant = new ArrayList<>();
        referenceVariant.child(keyProduct).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelVariants modelVariants = ds.getValue(ModelVariants.class);
                    if (modelVariants != null){
                        modelVariants.setKeyVariant(ds.getKey());
                        listVariant.add(modelVariants);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listVariant.size() != 0){
                        adapterManageVariant.setItem(listVariant);
                        adapterManageVariant.notifyDataSetChanged();
                    }else {
                        Toast.makeText(ManageVariant.this, "This product not yet variant", Toast.LENGTH_SHORT).show();
                    }
                }, 100);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageVariant.this, "onError : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addVariant(String variant){
        ModelVariants modelVariants = new ModelVariants(variant);
        String newKeyVariant = referenceVariant.child(keyProduct).push().getKey();
        assert newKeyVariant != null;
        referenceVariant.child(keyProduct).child(newKeyVariant).setValue(modelVariants).addOnSuccessListener(unused -> {
            Toast.makeText(ManageVariant.this, "Success add new variant", Toast.LENGTH_SHORT).show();

            if (listVariant.size() == 0){
                // set item adapter if the data set 0
                adapterManageVariant.setItem(listVariant);
            }

            modelVariants.setKeyVariant(newKeyVariant);
            listVariant.add(modelVariants);
            adapterManageVariant.notifyDataSetChanged();

        }).addOnFailureListener(e -> Toast.makeText(ManageVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteVariant(int pos) {
        ModelVariants modelVariants = listVariant.get(pos);
        referenceVariant.child(keyProduct).child(modelVariants.getKeyVariant()).removeValue()
                .addOnSuccessListener(unused -> deleteProductVariant(pos))
                .addOnFailureListener(e -> Toast.makeText(ManageVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void deleteProductVariant(int pos){
        // if variant < 1 then delete all product variant
        // if variant > 1 then delete selection variant in product variant

        if (listVariant.size() > 1){
            updateListDetailProductVariant(keyProduct, pos);
        }else {
            deleteAllProductVariant(keyProduct);
        }
        listVariant.remove(pos);
        adapterManageVariant.notifyItemRemoved(pos);

    }

    int indexData;
    private void deleteAllProductVariant(String key){
        // get data and delete image from storage
        referenceProductVariant.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long countChild = snapshot.getChildrenCount();
                indexData = 1;
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProductVariant modelProductVariant = ds.getValue(ModelProductVariant.class);
                    if (modelProductVariant != null){
                        String name_photo = referenceStorage.getReferenceFromUrl(modelProductVariant.getUrlImage()).getName();
                        StorageReference deleteRef = referenceStorage.getReference("product_variant/"+name_photo);

                        deleteRef.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(ManageVariant.this, indexData+""+countChild, Toast.LENGTH_SHORT).show();
                                if (indexData == countChild){
                                    // delete all product variant
                                    deleteProductVariantData(key);
                                }else {
                                    indexData += 1;
                                }
                            }else {
                                Toast.makeText(ManageVariant.this, "taskException : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageVariant.this, "onError : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProductVariantData(String key){
        referenceProductVariant.child(key).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(ManageVariant.this, "All data product is deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ManageVariant.this, "onError : "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateListDetailProductVariant(String key, int pos){
        referenceProductVariant.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long countChild = snapshot.getChildrenCount();
                indexData = 1;
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProductVariant modelProductVariant = ds.getValue(ModelProductVariant.class);
                    if (modelProductVariant != null){
                        ds.getRef().child("listDetailProductVariant").child(String.valueOf(pos)).removeValue();
                        if (indexData == countChild){
                            Toast.makeText(ManageVariant.this, "Success delete variant", Toast.LENGTH_SHORT).show();
                        }else {
                            indexData += 1;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageVariant.this, "onError : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
