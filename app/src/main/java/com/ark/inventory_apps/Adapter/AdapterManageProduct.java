package com.ark.inventory_apps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelProduct;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Inventory.Product.DetailProduct;
import com.ark.inventory_apps.View.Inventory.Product.EditProduct;
import com.ark.inventory_apps.View.Inventory.Product.ManageProductVariant;
import com.ark.inventory_apps.View.Inventory.Product.ManageVariant;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterManageProduct extends RecyclerView.Adapter<AdapterManageProduct.ManageProductVH> {

    private final Context context;
    private List<ModelProduct> listProduct = new ArrayList<>();
    private final ProgressBar progressCircular;

    // init ref root
    private final DatabaseReference referenceProduct = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product");
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");
    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");

    // init bottom sheet dialog
    private BottomSheetDialog bottomSheetDialog;

    // init storage firebase
    FirebaseStorage referenceStorage = FirebaseStorage.getInstance();

    public AdapterManageProduct(Context context, ProgressBar progressCircular) {
        this.context = context;
        this.progressCircular = progressCircular;
    }

    public void setItem(List<ModelProduct> listProduct){
        this.listProduct = listProduct;
    }

    @NonNull
    @Override
    public ManageProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_product, parent, false);
        return new ManageProductVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageProductVH holder, int position) {
        ModelProduct modelProduct = listProduct.get(position);

        Picasso.get().load(modelProduct.getUrlImage()).into(holder.imageProduct);
        holder.textProduct.setText(modelProduct.getNameProduct());
        holder.textPrice.setText(Functions.currencyRp(modelProduct.getPriceDefault()));

        holder.cardProduct.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(context);
            setProductBottomDialog(modelProduct, position);
            bottomSheetDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return listProduct.size();
    }

    public static class ManageProductVH extends RecyclerView.ViewHolder {
        CardView cardProduct;
        ImageView imageProduct;
        TextView textProduct, textPrice;
        public ManageProductVH(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.card_product);
            imageProduct = itemView.findViewById(R.id.image_product);
            textProduct = itemView.findViewById(R.id.text_product);
            textPrice = itemView.findViewById(R.id.text_price);
        }
    }

    private void setProductBottomDialog(ModelProduct modelProduct, int pos){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_product_settings, null, false);
        TextView viewDetailProduct, viewEditProduct, viewDeleteProduct, viewManageVariant, viewAddVariantProduct;
        viewDetailProduct = viewBottomDialog.findViewById(R.id.detail_product);
        viewEditProduct = viewBottomDialog.findViewById(R.id.edit_product);
        viewDeleteProduct = viewBottomDialog.findViewById(R.id.delete_product);
        viewManageVariant = viewBottomDialog.findViewById(R.id.manage_variant);
        viewAddVariantProduct = viewBottomDialog.findViewById(R.id.add_variant_product);

        viewDetailProduct.setOnClickListener(view -> {
            Intent intent = new Intent(context, DetailProduct.class);
            intent.putExtra("keyProduct", modelProduct.getKeyProduct());
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        viewEditProduct.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditProduct.class);
            intent.putExtra("keyProduct", modelProduct.getKeyProduct());
            intent.putExtra("oldImageProduct", modelProduct.getUrlImage());
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        viewDeleteProduct.setOnClickListener(view -> {
            confirmationDelete(modelProduct, pos);
            bottomSheetDialog.dismiss();
        });

        viewManageVariant.setOnClickListener(view -> {
            Intent intent = new Intent(context, ManageVariant.class);
            intent.putExtra("keyProduct", modelProduct.getKeyProduct());
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        viewAddVariantProduct.setOnClickListener(view -> {
            Intent intent = new Intent(context, ManageProductVariant.class);
            intent.putExtra("keyProduct", modelProduct.getKeyProduct());
            context.startActivity(intent);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void confirmationDelete(ModelProduct modelProduct, int pos){
        //Create the Dialog here
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_confirmation_delete);
        dialog.getWindow().setBackgroundDrawable(context.getDrawable(R.drawable.background_center_dialog));

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button Okay = dialog.findViewById(R.id.btn_okay);
        Button Cancel = dialog.findViewById(R.id.btn_cancel);

        dialog.show();
        Okay.setOnClickListener(v -> {
            deleteProduct(modelProduct, pos);
            dialog.dismiss();
        });

        Cancel.setOnClickListener(v -> dialog.dismiss());
    }



    private void deleteProduct(ModelProduct modelProduct, int pos){
        progressCircular.setVisibility(View.VISIBLE);

        String name_photo = referenceStorage.getReferenceFromUrl(modelProduct.getUrlImage()).getName();
        StorageReference deleteRef = referenceStorage.getReference("product/"+name_photo);

        String keyProduct = modelProduct.getKeyProduct();

        deleteRef.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                // delete product data
                referenceProduct.child(keyProduct).removeValue()
                        .addOnSuccessListener(unused -> {
                            deleteVariant(keyProduct);
                            listProduct.remove(pos);
                            notifyItemRemoved(pos);
                            progressCircular.setVisibility(View.GONE);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            progressCircular.setVisibility(View.GONE);
                        });
            }else {
                Toast.makeText(context, "Storage : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                progressCircular.setVisibility(View.GONE);
            }
        });
    }

    private void deleteVariant(String key){
        referenceVariant.child(key).removeValue()
                .addOnSuccessListener(unused -> deleteProductVariantImage(key))
                .addOnFailureListener(e -> {
                    progressCircular.setVisibility(View.GONE);
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    int indexData;
    private void deleteProductVariantImage(String key){
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
                                Toast.makeText(context, indexData+""+countChild, Toast.LENGTH_SHORT).show();
                                if (indexData == countChild){
                                    deleteProductVariantData(key);
                                }else {
                                    indexData += 1;
                                }
                            }else {
                                progressCircular.setVisibility(View.GONE);
                                Toast.makeText(context, "taskException : "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        progressCircular.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "onError : "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteProductVariantData(String key){
        referenceProductVariant.child(key).removeValue().addOnSuccessListener(unused -> {
            Toast.makeText(context, "All data product is deleted", Toast.LENGTH_SHORT).show();
            progressCircular.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "onError : "+e.getMessage(), Toast.LENGTH_SHORT).show();
            progressCircular.setVisibility(View.GONE);
        });

    }}
