package com.ark.inventory_apps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.ark.inventory_apps.Model.ModelCategory;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Inventory.Category.ManageSubCategory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdapterManageCategory extends RecyclerView.Adapter<AdapterManageCategory.ManageCategoryVH> {

    private List<ModelCategory> listCategory = new ArrayList<>();
    private final Context context;

    public BottomSheetDialog bottomSheetDialog;

    public AdapterManageCategory(Context context) {
        this.context = context;
        bottomSheetDialog = new BottomSheetDialog(context);
    }

    public void setItem(List<ModelCategory> listCategory){
        this.listCategory = listCategory;
    }

    @NonNull
    @Override
    public AdapterManageCategory.ManageCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_category, parent, false);
        return new ManageCategoryVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterManageCategory.ManageCategoryVH holder, int position) {
        ModelCategory modelCategory = listCategory.get(position);
        holder.textCategory.setText(modelCategory.getCategory());

        holder.cardCategory.setOnClickListener(view -> {
            Intent intent = new Intent(context, ManageSubCategory.class);
            intent.putExtra("key", modelCategory.getKeyCategory());
            intent.putExtra("category", modelCategory.getCategory());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return listCategory.size();
    }

    public static class ManageCategoryVH extends RecyclerView.ViewHolder {
        CardView cardCategory;
        TextView textCategory;
        public ManageCategoryVH(@NonNull View itemView) {
            super(itemView);

            cardCategory = itemView.findViewById(R.id.card_category);
            textCategory = itemView.findViewById(R.id.text_name_category);
        }
    }

    public void setActionBottomDialog(ModelCategory modelCategory, int pos){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_action_category, null, false);
        TextInputEditText categoryEt = viewBottomDialog.findViewById(R.id.category_add);
        categoryEt.setText(modelCategory.getCategory());

        MaterialButton editBtn = viewBottomDialog.findViewById(R.id.edit_btn);
        MaterialButton deleteBtn = viewBottomDialog.findViewById(R.id.delete_btn);

        editBtn.setOnClickListener(view -> {
            String category = Objects.requireNonNull(categoryEt.getText()).toString();
            updateDataCategory(modelCategory.getKeyCategory(), category, pos);
            bottomSheetDialog.dismiss();
        });

        deleteBtn.setOnClickListener(view -> {
            String category = Objects.requireNonNull(categoryEt.getText()).toString();

            if (category.isEmpty()){
                Toast.makeText(context, "Form category is empty", Toast.LENGTH_SHORT).show();
            }else {
                confirmationDelete(modelCategory.getKeyCategory(), pos);
            }

            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void updateDataCategory(String keyCategory, String category, int pos) {
        ModelCategory modelCategory = new ModelCategory(category);
        Variables.referenceRoot.child("inventory").child(Variables.uid).child("category").child(keyCategory).setValue(modelCategory)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Success update data category", Toast.LENGTH_SHORT).show();

                    // change category in list
                    listCategory.get(pos).setCategory(category);

                    // notify category change in adapter
                    notifyItemChanged(pos);

        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmationDelete(String keyCategory, int pos){
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
            // new data category
            deleteCategory(keyCategory, pos);
            dialog.dismiss();
        });

        Cancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void deleteCategory(String keyCategory, int pos){
        Variables.referenceRoot.child("inventory").child(Variables.uid).child("category").child(keyCategory).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context, "Success delete category", Toast.LENGTH_SHORT).show();

                // remove category in list
                listCategory.remove(pos);

                // notify category in adapter
                notifyItemRemoved(pos);
            }
        }).addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

    }
}
