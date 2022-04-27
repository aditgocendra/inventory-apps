package com.ark.inventory_apps.View.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivityAuthenticationBinding;

public class Authentication extends AppCompatActivity {

    private ActivityAuthenticationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();
    }

    private void listenerComponent() {
        binding.signInBtn.setOnClickListener(view -> Functions.updateUI(this, SignIn.class));
        binding.signUpBtn.setOnClickListener(view -> Functions.updateUI(this, SignUp.class));

    }
}