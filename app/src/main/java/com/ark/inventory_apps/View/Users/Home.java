package com.ark.inventory_apps.View.Users;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);


    }
}