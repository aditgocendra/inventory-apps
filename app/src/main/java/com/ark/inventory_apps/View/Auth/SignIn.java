package com.ark.inventory_apps.View.Auth;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.View.Users.Home;
import com.ark.inventory_apps.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Objects;

public class SignIn extends AppCompatActivity {

    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);

        listenerComponent();

    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());
        binding.recoverPassRedirect.setOnClickListener(view -> Functions.updateUI(this, ForgotPassword.class));

        binding.signInBtn.setOnClickListener(view -> {
            String email = binding.emailSignIn.getText().toString();
            String pass = binding.passSignIn.getText().toString();

            if (email.isEmpty()){
                Toast.makeText(SignIn.this, "Email is null", Toast.LENGTH_SHORT).show();
            }else if (pass.isEmpty()){
                Toast.makeText(SignIn.this, "Password is null", Toast.LENGTH_SHORT).show();
            }else {
                binding.signInBtn.setEnabled(false);
                binding.progressCircular.setVisibility(View.VISIBLE);
                signIn(email, pass);
            }
        });
    }

    private void signIn(String email, String pass) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pass).addOnSuccessListener(authResult -> {
            if (Objects.requireNonNull(authResult.getUser()).isEmailVerified()){
                FirebaseUser user = authResult.getUser();
                Variables.uid = user.getUid();

                Functions.updateUI(SignIn.this, Home.class);
                finish();
            }else {
                Toast.makeText(SignIn.this, "Your email is not verified, please verify and comeback.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(SignIn.this, e.getMessage(), Toast.LENGTH_SHORT).show());

        binding.signInBtn.setEnabled(true);
        binding.progressCircular.setVisibility(View.GONE);
    }
}