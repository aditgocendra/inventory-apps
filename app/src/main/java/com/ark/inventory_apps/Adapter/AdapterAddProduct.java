package com.ark.inventory_apps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Model.ModelVariants;
import com.ark.inventory_apps.R;
import java.util.ArrayList;
import java.util.List;

public class AdapterAddProduct extends RecyclerView.Adapter<AdapterAddProduct.VariantProductVH> {

    private final Context context;
    private List<ModelVariants> listVariants = new ArrayList<>();

    public AdapterAddProduct(Context context) {
        this.context = context;
    }

    public void setItem(List<ModelVariants> listVariants){
        this.listVariants = listVariants;
    }

    @NonNull
    @Override
    public VariantProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_variant_product, parent, false);
        return new VariantProductVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VariantProductVH holder, int position) {
        ModelVariants modelVariants = listVariants.get(position);
        holder.textVariants.setText(modelVariants.getNameVariant());
    }

    @Override
    public int getItemCount() {
        return listVariants.size();
    }

    public static class VariantProductVH extends RecyclerView.ViewHolder {
        CardView cardVariants;
        TextView textVariants;
        public VariantProductVH(@NonNull View itemView) {
            super(itemView);
            cardVariants = itemView.findViewById(R.id.card_variants_product);
            textVariants = itemView.findViewById(R.id.name_variants_text);

        }
    }
}
