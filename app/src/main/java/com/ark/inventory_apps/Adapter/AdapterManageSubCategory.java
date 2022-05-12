package com.ark.inventory_apps.Adapter;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelSubCategory;
import com.ark.inventory_apps.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import java.util.List;
import java.util.Objects;

public class AdapterManageSubCategory extends RecyclerView.Adapter<AdapterManageSubCategory.ManageSubCategoryVH> {

    private final Context context;
    private final List<ModelSubCategory> listSubCategory;
    private final String keyCategory;

    private final DatabaseReference referenceSubCategory = Variables.referenceRoot.child("inventory").child(Variables.uid).child("sub_category");
    private BottomSheetDialog bottomSheetDialog;

    public AdapterManageSubCategory(Context context, List<ModelSubCategory> listSubCategory, String keyCategory) {
        this.context = context;
        this.listSubCategory = listSubCategory;
        this.keyCategory = keyCategory;
    }

    @NonNull
    @Override
    public ManageSubCategoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.layout_card_sub_category, parent, false);
        return new ManageSubCategoryVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageSubCategoryVH holder, int position) {
        ModelSubCategory modelSubCategory = listSubCategory.get(position);

        holder.textSubCategory.setText(modelSubCategory.getSubCategory());
        holder.deleteBtn.setOnClickListener(view -> confirmationDelete(modelSubCategory.getKeySubCategory()));
        holder.editBtn.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(context);
            setEditBottomDialog(modelSubCategory);
            bottomSheetDialog.show();
        });

    }

    @Override
    public int getItemCount() {
        return listSubCategory.size();
    }

    public static class ManageSubCategoryVH extends RecyclerView.ViewHolder {
        LinearLayout editBtn, deleteBtn;
        TextView textSubCategory;
        public ManageSubCategoryVH(@NonNull View itemView) {
            super(itemView);

            editBtn = itemView.findViewById(R.id.edit_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);
            textSubCategory = itemView.findViewById(R.id.sub_category_text);
        }
    }

    public void setEditBottomDialog(ModelSubCategory modelSubCategory){
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View viewBottomDialog = layoutInflater.inflate(R.layout.layout_edit_sub_category, null, false);
        TextInputEditText subCategoryEt = viewBottomDialog.findViewById(R.id.sub_category_edit);
        subCategoryEt.setText(modelSubCategory.getSubCategory());

        MaterialButton editBtn = viewBottomDialog.findViewById(R.id.edit_btn);

        editBtn.setOnClickListener(view -> {
            String subCategory = Objects.requireNonNull(subCategoryEt.getText()).toString();
            updateDataSubCategory(modelSubCategory.getKeySubCategory(), subCategory);
            bottomSheetDialog.dismiss();
        });


        bottomSheetDialog.setContentView(viewBottomDialog);
    }

    private void updateDataSubCategory(String keySubCategory, String subCategory) {
        ModelSubCategory modelSubCategory = new ModelSubCategory(subCategory);
        referenceSubCategory.child(keyCategory).child(keySubCategory).setValue(modelSubCategory)
                .addOnSuccessListener(unused -> Toast.makeText(context, "Success update data sub category", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void confirmationDelete(String keySubCategory){
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
            // delete sub category
            deleteSubCategory(keySubCategory);
            dialog.dismiss();
        });

        Cancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void deleteSubCategory(String keySubCategory) {
        referenceSubCategory.child(keyCategory).child(keySubCategory).removeValue()
                .addOnSuccessListener(unused -> Toast.makeText(context, "Success delete sub category", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
