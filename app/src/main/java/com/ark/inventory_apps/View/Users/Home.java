package com.ark.inventory_apps.View.Users;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Auth.Authentication;
import com.ark.inventory_apps.View.Inventory.Category.ManageCategory;
import com.ark.inventory_apps.View.Inventory.Product.ManageProduct;
import com.ark.inventory_apps.databinding.ActivityHomeBinding;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

    }

    private void listenerComponent() {
        // nav drawer
        binding.navbarBtn.setOnClickListener(view -> binding.drawerLayout.open());
        binding.navView.bringToFront();
        binding.navView.setNavigationItemSelectedListener(this);

        // chart dashboard
        setChartInventory();

    }

    private void setChartInventory(){
        // Initialize entries data and color used
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // add color used
        colors.add(getResources().getColor(R.color.primary_dark));
        colors.add(getResources().getColor(R.color.primary));

        // set data entries
        pieEntries.add(new PieEntry(100, "Product In"));
        pieEntries.add(new PieEntry(70, "Product Out"));
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");

        // custom set data attributes
        pieDataSet.setColors(colors);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(12f);

        // custom attr pie chart
        binding.chartDashboard.setCenterText("Chart");
        binding.chartDashboard.setCenterTextColor(Color.BLACK);
        binding.chartDashboard.setHoleColor(Color.TRANSPARENT);
        binding.chartDashboard.setData(new PieData(pieDataSet));
        binding.chartDashboard.animateXY(1500, 1500);
        binding.chartDashboard.getDescription().setEnabled(false);
        binding.chartDashboard.setEntryLabelTextSize(12f);
        binding.chartDashboard.setEntryLabelColor(Color.BLACK);
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.home:
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            case R.id.manage_product:
                Functions.updateUI(this, ManageProduct.class);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            case R.id.product_in:
                break;
            case R.id.product_out:
                break;
            case R.id.category:
                Functions.updateUI(this, ManageCategory.class);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            case R.id.about:
                break;
            case R.id.share:
                break;
            case R.id.settings:
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Functions.updateUI(this, Authentication.class);
                binding.drawerLayout.closeDrawer(GravityCompat.START);
                finish();
        }

        return true;
    }
}