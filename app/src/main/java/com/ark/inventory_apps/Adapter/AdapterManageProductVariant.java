package com.ark.inventory_apps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Inventory.Product.EditProductVariant;
import com.ark.inventory_apps.View.Inventory.Product.ManageProductVariant;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.List;

public class AdapterManageProductVariant extends RecyclerView.Adapter<AdapterManageProductVariant.ManageProductVariantVH> {

    private final Context context;
    private final List<ModelProductVariant> listProductVariant;
    private final String keyProduct;

    private final DatabaseReference referenceProductVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("product_variant");

    public AdapterManageProductVariant(Context context, List<ModelProductVariant> listProductVariant, String keyProduct) {
        this.context = context;
        this.listProductVariant = listProductVariant;
        this.keyProduct = keyProduct;
    }

    @NonNull
    @Override
    public ManageProductVariantVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_grid_product_variant, parent, false);
        return new ManageProductVariantVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageProductVariantVH holder, int position) {
        ModelProductVariant modelProductVariant = listProductVariant.get(position);
        Picasso.get().load(modelProductVariant.getUrlImage()).into(holder.imageProductVariant);

        holder.textPriceVariant.setText(Functions.currencyRp(modelProductVariant.getPriceVariant()));

        holder.cardDelete.setOnClickListener(view -> {
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
                deleteProductVariant(modelProductVariant);
                dialog.dismiss();
            });

            Cancel.setOnClickListener(v -> dialog.dismiss());
        });

        holder.cardEdit.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditProductVariant.class);
            intent.putExtra("keyProduct", keyProduct);
            intent.putExtra("keyProductVariant", modelProductVariant.getKeyProductVariant());
            intent.putExtra("oldImageProductVariant", modelProductVariant.getUrlImage());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listProductVariant.size();
    }

    public static class ManageProductVariantVH extends RecyclerView.ViewHolder {
        ImageView imageProductVariant;
        TextView textPriceVariant;
        CardView cardEdit, cardDelete;
        public ManageProductVariantVH(@NonNull View itemView) {
            super(itemView);
            imageProductVariant = itemView.findViewById(R.id.image_product_variant);
            textPriceVariant = itemView.findViewById(R.id.price_product_variant);
            cardEdit = itemView.findViewById(R.id.card_edit_product_variant);
            cardDelete = itemView.findViewById(R.id.card_delete_product_variant);
        }
    }

    private void deleteProductVariant(ModelProductVariant modelProductVariant) {
        FirebaseStorage referenceStorage = FirebaseStorage.getInstance();
        String name_photo = referenceStorage.getReferenceFromUrl(modelProductVariant.getUrlImage()).getName();
        StorageReference deleteRef = referenceStorage.getReference("product_variant/"+name_photo);

        deleteRef.delete().addOnSuccessListener(unused -> referenceProductVariant.child(keyProduct).child(modelProductVariant.getKeyProductVariant()).removeValue()
                .addOnSuccessListener(unused1 -> Toast.makeText(context, "Success delete product", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show())).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }




}
