package com.ark.inventory_apps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

public class AdapterFormVariant extends RecyclerView.Adapter<AdapterFormVariant.FormVariantVH> {

    private final Context context;
    private final List<ModelVariants> listVariant;

    public AdapterFormVariant(Context context, List<ModelVariants> listVariant) {
        this.context = context;
        this.listVariant = listVariant;
    }

    @NonNull
    @Override
    public FormVariantVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_form_variant, parent, false);
        return new FormVariantVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FormVariantVH holder, int position) {
        ModelVariants modelVariants = listVariant.get(position);
        holder.layoutForm.setHint(modelVariants.getNameVariant());
    }

    @Override
    public int getItemCount() {
        return listVariant.size();
    }

    public static class FormVariantVH extends RecyclerView.ViewHolder {
        TextInputEditText typeVariant;
        TextInputLayout layoutForm;
        public FormVariantVH(@NonNull View itemView) {
            super(itemView);
            layoutForm = itemView.findViewById(R.id.layout_form);
            typeVariant = itemView.findViewById(R.id.type_variant);

        }
    }


}
