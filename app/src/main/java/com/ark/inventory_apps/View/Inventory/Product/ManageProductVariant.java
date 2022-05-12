package com.ark.inventory_apps.View.Inventory.Product;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterFormVariant;
import com.ark.inventory_apps.Adapter.AdapterManageProductVariant;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelDetailProductVariant;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Auth.SignIn;
import com.ark.inventory_apps.View.Auth.SignUp;
import com.ark.inventory_apps.databinding.ActivityManageProductVariantBinding;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ManageProductVariant extends AppCompatActivity {

    // init binding activity
    private ActivityManageProductVariantBinding binding;

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;

    // init ref root variant
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");
    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");

    // init key product from intent
    private String keyProduct;

    // init adapter form variant
    private AdapterFormVariant adapterFormVariant;
    private List<ModelVariants> listVariant;

    // init adapter form manage product variant
    private AdapterManageProductVariant adapterManageProductVariant;
    private List<ModelProductVariant> listProductVariant;

    // pick image setup
    private ActivityResultLauncher<String> imagePick;
    private boolean onImageChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageProductVariantBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyProduct = getIntent().getStringExtra("keyProduct");

        listenerComponent();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.recyclerManageProductVariant.setLayoutManager(gridLayoutManager);
        binding.recyclerManageProductVariant.setHasFixedSize(true);

        setDataProductVariant();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        bottomSheetDialog = new BottomSheetDialog(this);
        setAddBottomDialog();
        binding.floatAddProductVariant.setOnClickListener(view -> bottomSheetDialog.show());
    }

    public void setAddBottomDialog(){
        View viewBottomDialog = getLayoutInflater().inflate(R.layout.layout_add_product_variant, null, false);
        RecyclerView recyclerFormVariant = viewBottomDialog.findViewById(R.id.recycler_form_variant);
        ImageView productVariantImage = viewBottomDialog.findViewById(R.id.product_variant_image);
        TextInputEditText textPriceVariant = viewBottomDialog.findViewById(R.id.price_variant_add);
        TextInputEditText textStockVariant = viewBottomDialog.findViewById(R.id.stock_variant);
        Button finishBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        // layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        // set layout recycler variant product
        recyclerFormVariant.setLayoutManager(layoutManager);
        recyclerFormVariant.setItemAnimator(new DefaultItemAnimator());

        imagePick = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                result -> {
                    productVariantImage.setImageURI(result);
                    onImageChange = true;
                }
        );

        productVariantImage.setOnClickListener(view -> imagePick.launch("image/*"));

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
                    adapterFormVariant = new AdapterFormVariant(ManageProductVariant.this, listVariant);
                    recyclerFormVariant.setAdapter(adapterFormVariant);
                }else {
                    Log.d("test", "null variant");
                    //Create the dialog if variant null
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.layout_confirmation_warning);
                    dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_center_dialog));

                    dialog.getWindow().setLayout(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    dialog.setCancelable(false); //Optional
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

                    Button Okay = dialog.findViewById(R.id.btn_okay);
                    TextView textInformation = dialog.findViewById(R.id.textView2);
                    textInformation.setText("This product not yet variant product, please insert variant this product and comeback");

                    dialog.show();

                    Okay.setOnClickListener(v -> {
                        dialog.dismiss();
                        finish();
                    });

                }
            },100);


        }).addOnFailureListener(e -> Toast.makeText(ManageProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        // listen finish btn add new product variant
        finishBtn.setOnClickListener(view -> {
            List<ModelDetailProductVariant> listDetailProductVariant;
            if (listVariant.size() != 0){
                int totalVariant = listVariant.size();
                listDetailProductVariant = new ArrayList<>();

                for (int i=0; i<totalVariant; i++){
                    View itemView = Objects.requireNonNull(recyclerFormVariant.findViewHolderForLayoutPosition(i)).itemView;
                    TextInputEditText typeVariant = itemView.findViewById(R.id.type_variant);

                    String variantType = Objects.requireNonNull(typeVariant.getText()).toString();

                    if (variantType.isEmpty()){
                        bottomSheetDialog.dismiss();
                        Toast.makeText(this, "Variant type not complete", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ModelDetailProductVariant modelDetailProductVariant = new ModelDetailProductVariant(variantType);
                    modelDetailProductVariant.setKeyVariant(listVariant.get(i).getKeyVariant());
                    listDetailProductVariant.add(modelDetailProductVariant);

                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listDetailProductVariant.size() != 0){
                        String priceVariant = Objects.requireNonNull(textPriceVariant.getText()).toString();
                        String stockVariant = Objects.requireNonNull(textStockVariant.getText()).toString();

                        // valid form
                        if (priceVariant.isEmpty()){
                            Toast.makeText(this, "Price variant is empty", Toast.LENGTH_SHORT).show();
                        }else if (stockVariant.isEmpty()){
                            Toast.makeText(this, "Stock Variant is empty", Toast.LENGTH_SHORT).show();
                        }else if (!onImageChange){
                            Toast.makeText(this, "Image not selected", Toast.LENGTH_SHORT).show();
                        }else {
                            binding.progressCircular.setVisibility(View.VISIBLE);
                            Bitmap bitmap = ((BitmapDrawable) productVariantImage.getDrawable()).getBitmap();
                            saveProductVariant(priceVariant, stockVariant, bitmap, listDetailProductVariant);
                        }

                        bottomSheetDialog.dismiss();
                    }else {
                        Toast.makeText(ManageProductVariant.this, "Variant is empty", Toast.LENGTH_SHORT).show();
                    }
                }, 100);

            }

        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void saveProductVariant(String priceVariant, String stockVariant, Bitmap bitmap, List<ModelDetailProductVariant> listDetailProductVariant){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
        Date now = new Date();
        String fileName = dateFormat.format(now);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("product_variant/"+fileName);

        ByteArrayOutputStream baOs = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baOs);
        byte[] data = baOs.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.addOnFailureListener(e -> {
            binding.progressCircular.setVisibility(View.GONE);
            Toast.makeText(ManageProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {

                    ModelProductVariant modelProductVariant = new ModelProductVariant(
                            priceVariant,
                            Integer.parseInt(stockVariant),
                            String.valueOf(uri),
                            listDetailProductVariant

                    );

                    referenceProductVariant.child(keyProduct).push().setValue(modelProductVariant)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(ManageProductVariant.this, "Success add new product variant", Toast.LENGTH_SHORT).show();
                                binding.progressCircular.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ManageProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                binding.progressCircular.setVisibility(View.GONE);
                            });

                        })
                .addOnFailureListener(e -> {
                    binding.progressCircular.setVisibility(View.GONE);
                    Toast.makeText(ManageProductVariant.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    private void setDataProductVariant() {
        referenceProductVariant.child(keyProduct).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProductVariant = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProductVariant modelProductVariant = ds.getValue(ModelProductVariant.class);
                    if (modelProductVariant != null){
                        modelProductVariant.setKeyProductVariant(ds.getKey());
                        listProductVariant.add(modelProductVariant);
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listProductVariant.size() != 0){
                        adapterManageProductVariant = new AdapterManageProductVariant(ManageProductVariant.this, listProductVariant, keyProduct);
                        binding.recyclerManageProductVariant.setAdapter(adapterManageProductVariant);
                    }else {
                        Toast.makeText(ManageProductVariant.this, "Not yet product variant", Toast.LENGTH_SHORT).show();
                    }
                }, 300);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageProductVariant.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}