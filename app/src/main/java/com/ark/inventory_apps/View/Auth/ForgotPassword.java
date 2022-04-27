package com.ark.inventory_apps.View.Auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);
        
        listenerComponent();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.forgotPassBtn.setOnClickListener(view -> {
            String email = binding.emailForgotPass.getText().toString();
            if (email.isEmpty()){
                Toast.makeText(ForgotPassword.this, "Email is null", Toast.LENGTH_SHORT).show();
            }else {
                binding.forgotPassBtn.setEnabled(false);
                binding.progressCircular.setVisibility(View.VISIBLE);
                forgotPass(email);
            }
        });
    }
    
    private void forgotPass(String email){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> Toast.makeText(ForgotPassword.this, "We have sent assistance via your email, please verify and you will be able to use your account again", Toast.LENGTH_LONG).show())
                .addOnFailureListener(e -> Toast.makeText(ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        binding.forgotPassBtn.setEnabled(true);
        binding.progressCircular.setVisibility(View.GONE);
    }
}