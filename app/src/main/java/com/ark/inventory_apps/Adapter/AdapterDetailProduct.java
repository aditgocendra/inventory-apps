package com.ark.inventory_apps.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Model.ModelProductVariant;
import com.ark.inventory_apps.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterDetailProduct extends RecyclerView.Adapter<AdapterDetailProduct.DetailProductVH> {

    private final Context context;
    private final List<ModelProductVariant> listProductVariant;

    public AdapterDetailProduct(Context context, List<ModelProductVariant> listProductVariant) {
        this.context = context;
        this.listProductVariant = listProductVariant;
    }

    @NonNull
    @Override
    public DetailProductVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_product_variant_detail, parent, false);
        return new DetailProductVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailProductVH holder, int position) {
        ModelProductVariant modelProductVariant = listProductVariant.get(position);
        Picasso.get().load(modelProductVariant.getUrlImage()).into(holder.imageProductVariant);
        holder.priceProductVariant.setText(Functions.currencyRp(modelProductVariant.getPriceVariant()));
        holder.stockProductVariant.setText(String.valueOf(modelProductVariant.getStockVariant()));
    }

    @Override
    public int getItemCount() {
        return listProductVariant.size();
    }

    public static class DetailProductVH extends RecyclerView.ViewHolder {
        ImageView imageProductVariant;
        TextView priceProductVariant, stockProductVariant;
        public DetailProductVH(@NonNull View itemView) {
            super(itemView);
            imageProductVariant = itemView.findViewById(R.id.image_product_variant);
            priceProductVariant = itemView.findViewById(R.id.price_product_variant);
            stockProductVariant = itemView.findViewById(R.id.stock_product_variant);
        }
    }
}
