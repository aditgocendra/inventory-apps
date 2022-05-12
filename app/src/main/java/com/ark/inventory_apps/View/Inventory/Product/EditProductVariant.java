package com.ark.inventory_apps.View.Inventory.Product;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterFormVariant;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelDetailProductVariant;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityEditProductVariantBinding;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class EditProductVariant extends AppCompatActivity {

    // init binding activity
    private ActivityEditProductVariantBinding binding;

    // init ref root variant
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");
    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");

    // init adapter form variant
    private AdapterFormVariant adapterFormVariant;
    private List<ModelVariants> listVariant;

    // init string extra
    private String keyProduct;
    private String keyProductVariant;
    private String oldImageUrl;

    // init pick image setup
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    // list detail product variant
    List<ModelDetailProductVariant> listAdapterDetailProductVariant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProductVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        // get intent data
        keyProduct = getIntent().getStringExtra("keyProduct");
        keyProductVariant = getIntent().getStringExtra("keyProductVariant");
        oldImageUrl = getIntent().getStringExtra("oldImageProductVariant");

        listenerComponent();

        // layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        // set layout recycler variant product
        binding.recyclerFormVariant.setLayoutManager(layoutManager);
        binding.recyclerFormVariant.setItemAnimator(new DefaultItemAnimator());

        setFormVariant();
        pickImageSetup();

    }

    private void listenerComponent() {
        // set enable btn edit product variant false
        binding.editProductVariantBtn.setEnabled(false);

        binding.backBtn.setOnClickListener(view -> finish());

        binding.productVariantImage.setOnClickListener(view -> imagePick.launch("image/*"));

        binding.editProductVariantBtn.setOnClickListener(view -> {
            String priceVariant = Objects.requireNonNull(binding.priceVariantEdit.getText()).toString();
            String stockVariant = Objects.requireNonNull(binding.stockVariantEdit.getText()).toString();

            int totalVariant = listVariant.size();
            List<ModelDetailProductVariant> listDetailProductVariant = new ArrayList<>();

            for (int i=0; i<totalVariant; i++){
                View itemView = Objects.requireNonNull(binding.recyclerFormVariant.findViewHolderForLayoutPosition(i)).itemView;
                TextInputEditText typeVariant = itemView.findViewById(R.id.type_variant);

                String variantType = Objects.requireNonNull(typeVariant.getText()).toString();

                if (variantType.isEmpty()){
                    Toast.makeText(this, "Variant type not complete", Toast.LENGTH_SHORT).show();
                    return;
                }

                ModelDetailProductVariant modelDetailProductVariant = new ModelDetailProductVariant(variantType);
                modelDetailProductVariant.setKeyVariant("-");
                listDetailProductVariant.add(modelDetailProductVariant);

            }

            if (priceVariant.isEmpty()){
                Toast.makeText(this, "Price variant is empty", Toast.LENGTH_SHORT).show();
            }else if (stockVariant.isEmpty()){
                Toast.makeText(this, "Stock variant is empty", Toast.LENGTH_SHORT).show();
            } else {
                binding.progressCircular.setVisibility(View.VISIBLE);
//                Toast.makeText(this, String.valueOf(totalVariant), Toast.LENGTH_SHORT).show();
                if (onImageChange){
                    editProductVariantWithImage(priceVariant, stockVariant, listDetailProductVariant);
                }else {
                    editProductVariant(priceVariant, stockVariant, listDetailProductVariant, oldImageUrl);
                }

            }
        });

    }

    private void pickImageSetup(){
        imagePick = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    binding.productVariantImage.setImageURI(result);
                    onImageChange = true;
                }
        );
    }

    private void setFormVariant(){
        referenceVariant.child(keyProduct).get().addOnSuccessListener(dataSnapshot -> {
            listVariant = new ArrayList<>();
            for (DataSnapshot ds : dataSnapshot.getChildren()){
                ModelVariants modelVariants = ds.getValue(ModelVariants.class);
                if (modelVariants != null){
                    modelVariants.setKeyVariant(ds.getKey());
                    listVariant.add(modelVariants);
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if (listVariant.size() != 0){
                    adapterFormVariant = new AdapterFormVariant(EditProductVariant.this, listVariant);
                    binding.recyclerFormVariant.setAdapter(adapterFormVariant);
                    setDataProductVariant();
                }else {
                    Toast.makeText(this, "This product not yet variant product", Toast.LENGTH_SHORT).show();
                }
                // set enable btn product variant true
                binding.editProductVariantBtn.setEnabled(true);
            },100);


        }).addOnFailureListener(e -> {
            Toast.makeText(EditProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setDataProductVariant() {
        referenceProductVariant.child(keyProduct).child(keyProductVariant).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProductVariant modelProductVariant = task.getResult().getValue(ModelProductVariant.class);
                if (modelProductVariant != null){
                    modelProductVariant.setKeyProductVariant(task.getResult().getKey());

                    // set data to component
                    Picasso.get().load(modelProductVariant.getUrlImage()).into(binding.productVariantImage);
                    binding.priceVariantEdit.setText(modelProductVariant.getPriceVariant());
                    binding.stockVariantEdit.setText(String.valueOf(modelProductVariant.getStockVariant()));

                    listAdapterDetailProductVariant = modelProductVariant.getListDetailProductVariant();
                    if (listAdapterDetailProductVariant != null){
                        for (int i = 0; i < listAdapterDetailProductVariant.size(); i++){
                            View itemView = Objects.requireNonNull(binding.recyclerFormVariant.findViewHolderForLayoutPosition(i)).itemView;
                            TextInputEditText typeVariant = itemView.findViewById(R.id.type_variant);
                            typeVariant.setText(listAdapterDetailProductVariant.get(i).getTypeVariant());
                        }
                    }

                }
            }else {
                Toast.makeText(EditProductVariant.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editProductVariantWithImage(String priceVariant, String stockVariant, List<ModelDetailProductVariant> listDetailProductVariant) {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(oldImageUrl).getName();
        StorageReference deleteRef = referenceStorage.getReference("product_variant/"+name_photo);

        deleteRef.delete().addOnSuccessListener(unused -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
            Date now = new Date();
            String fileName = dateFormat.format(now);

            StorageReference storageRef = FirebaseStorage.getInstance().getReference("product_variant/"+fileName);

            Bitmap bitmap = ((BitmapDrawable) binding.productVariantImage.getDrawable()).getBitmap();
            ByteArrayOutputStream baOs = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
            byte[] data = baOs.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);

            uploadTask
                    .addOnFailureListener(e -> {
                        binding.progressCircular.setVisibility(View.GONE);
                        Toast.makeText(EditProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> editProductVariant(priceVariant, stockVariant, listDetailProductVariant, String.valueOf(uri)))
                                .addOnFailureListener(e -> {
                                    binding.progressCircular.setVisibility(View.GONE);
                                    Toast.makeText(EditProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });

        }).addOnFailureListener(e -> Toast.makeText(EditProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private void editProductVariant(String priceVariant, String stockVariant, List<ModelDetailProductVariant> listDetailProductVariant, String urlImage) {
        ModelProductVariant modelProductVariant = new ModelProductVariant(
                priceVariant,
                Integer.parseInt(stockVariant),
                urlImage,
                listDetailProductVariant
        );

        referenceProductVariant.child(keyProduct).child(keyProductVariant).setValue(modelProductVariant).addOnSuccessListener(unused -> {
            Toast.makeText(EditProductVariant.this, "Success edit product variant data", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> Toast.makeText(EditProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show());
        binding.progressCircular.setVisibility(View.GONE);
    }
}