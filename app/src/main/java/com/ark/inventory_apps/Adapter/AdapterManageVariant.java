package com.ark.inventory_apps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterManageVariant extends RecyclerView.Adapter<AdapterManageVariant.ManageVariantVH> {

    private final Context context;
    private final String keyProduct;

    private List<ModelVariants> listVariants = new ArrayList<>();
    private BottomSheetDialog bottomSheetDialog;

    // init ref root variant
    private final DatabaseReference referenceVariant = Variables.referenceRoot.child("inventory").child(Variables.uid).child("variant");

    public AdapterManageVariant(Context context, String keyProduct) {
        this.context = context;
        this.keyProduct = keyProduct;
    }

    public void setItem(List<ModelVariants> listVariants){
        this.listVariants = listVariants;
    }

    @NonNull
    @Override
    public ManageVariantVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_variant_product, parent, false);
        return new ManageVariantVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageVariantVH holder, int position) {
        ModelVariants modelVariants = listVariants.get(position);
        holder.textVariants.setText(modelVariants.getNameVariant());

        holder.cardVariants.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(context);
            setEditBottomDialog(modelVariants, position);
            bottomSheetDialog.show();

        });
    }

    @Override
    public int getItemCount() {
        return listVariants.size();
    }

    public static class ManageVariantVH extends RecyclerView.ViewHolder {
        CardView cardVariants;
        TextView textVariants;
        public ManageVariantVH(@NonNull View itemView) {
            super(itemView);

            cardVariants = itemView.findViewById(R.id.card_variants_product);
            textVariants = itemView.findViewById(R.id.name_variants_text);
        }
    }

    private void setEditBottomDialog(ModelVariants modelVariants, int pos){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_edit_variant, null, false);
        TextInputEditText nameVariantTi = viewBottomDialog.findViewById(R.id.name_variant_add);
        Button editBtn = viewBottomDialog.findViewById(R.id.finish_btn);

        nameVariantTi.setText(modelVariants.getNameVariant());

        editBtn.setOnClickListener(view -> {
            String newNameVariant = Objects.requireNonNull(nameVariantTi.getText()).toString();

            if (newNameVariant.isEmpty()){
                Toast.makeText(context, "Variant name is empty", Toast.LENGTH_SHORT).show();
            }else {
                // edit new variant
                editVariant(newNameVariant, modelVariants.getKeyVariant(), pos);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void editVariant(String newNameVariant, String keyVariant, int pos) {
        ModelVariants modelVariants = new ModelVariants(newNameVariant);
        referenceVariant.child(keyProduct).child(keyVariant).setValue(modelVariants)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Update variant success", Toast.LENGTH_SHORT).show();
                    listVariants.remove(pos);
                    notifyItemChanged(pos);
                })
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
