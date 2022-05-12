package com.ark.inventory_apps.View.Inventory.Product;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import com.ark.inventory_apps.Adapter.AdapterDetailProduct;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.Model.ModelProduct;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.databinding.ActivityDetailProductBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetailProduct extends AppCompatActivity {

    // init binding activity detail product
    private ActivityDetailProductBinding binding;

    // init ref root database
    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");
    private final DatabaseReference referenceProduct = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product");
    private final DatabaseReference referenceCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("category");
    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");

    // init string extra
    private String keyProduct;

    // init adapter
    private AdapterDetailProduct adapterDetailProduct;
    private List<ModelProductVariant> listProductVariant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        keyProduct = getIntent().getStringExtra("keyProduct");

        listenerComponent();

        RecyclerView.LayoutManager layoutManagerProduct = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerVariantProduct.setHasFixedSize(true);
        binding.recyclerVariantProduct.setLayoutManager(layoutManagerProduct);

        setDataProduct();

    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
    }

    private void setDataProduct() {
        referenceProduct.child(keyProduct).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelProduct modelProduct = task.getResult().getValue(ModelProduct.class);
                if (modelProduct != null){
                    modelProduct.setKeyProduct(modelProduct.getKeyProduct());

                    // set data to component
                    Picasso.get().load(modelProduct.getUrlImage()).into(binding.imageProductDetail);
                    binding.productNameText.setText(modelProduct.getNameProduct());
                    binding.priceProductText.setText(Functions.currencyRp(modelProduct.getPriceDefault()));
                    binding.stockProduct.setText("Stock : "+modelProduct.getStock());

                    setDataCategory(modelProduct.getKeyCategory(), modelProduct.getKeySubCategory());
                    setDataProductVariant();


                }else {
                    Toast.makeText(DetailProduct.this, "Product not found", Toast.LENGTH_SHORT).show();
                }
            }else {
                Toast.makeText(DetailProduct.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDataCategory(String keyCategory, String keySubCategory) {
        // set category
        referenceCategory.child(keyCategory).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                ModelCategory modelCategory = task.getResult().getValue(ModelCategory.class);
                if (modelCategory != null){
                    binding.categoryProductText.setText(modelCategory.getCategory());
                }else {
                    binding.categoryProductText.setTextColor(Color.RED);
                    binding.categoryProductText.setText("Category not found");
                }
            }else{
                Toast.makeText(DetailProduct.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // set sub category
        if (keySubCategory.equals("-")){
            binding.subCategoryProductText.setText("-");
        }else {
            referenceSubCategory.child(keyCategory).child(keySubCategory).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    ModelSubCategory modelSubCategory = task.getResult().getValue(ModelSubCategory.class);
                    if (modelSubCategory != null){
                        binding.subCategoryProductText.setText(modelSubCategory.getSubCategory());
                    }else {
                        Toast.makeText(DetailProduct.this, "Sub category not found", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(DetailProduct.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    int allStockVariant = 0;
    private void setDataProductVariant() {
        referenceProductVariant.child(keyProduct).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listProductVariant = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()){
                    ModelProductVariant modelProductVariant = ds.getValue(ModelProductVariant.class);
                    if (modelProductVariant != null){
                        modelProductVariant.setKeyProductVariant(ds.getKey());
                        listProductVariant.add(modelProductVariant);
                        allStockVariant += modelProductVariant.getStockVariant();
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (listProductVariant.size() != 0){
                        adapterDetailProduct = new AdapterDetailProduct(DetailProduct.this, listProductVariant);
                        binding.recyclerVariantProduct.setAdapter(adapterDetailProduct);

                        // set stock with calculate all stock variant product
                        binding.stockProduct.setText("Stock : "+ allStockVariant);

                    }else {
                        Toast.makeText(DetailProduct.this, "This product not yet product variant", Toast.LENGTH_SHORT).show();
                    }
                }, 100);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailProduct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}