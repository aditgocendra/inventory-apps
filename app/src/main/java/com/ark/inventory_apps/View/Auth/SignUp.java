package com.ark.inventory_apps.View.Auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.Model.ModelAccount;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.databinding.ActivitySignUpBinding;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Objects;

public class SignUp extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Functions.checkWindowSetFlag(this);
        listenerComponent();
    }

    private void listenerComponent() {
        binding.backBtn.setOnClickListener(view -> finish());

        binding.signUpBtn.setOnClickListener(view -> {
            String username = binding.usernameSignUp.getText().toString();
            String email = binding.emailSignUp.getText().toString();
            String pass = binding.passSignUp.getText().toString();
            String confirmPass = binding.confirmPassSignUp.getText().toString();

            if (email.isEmpty()){
                Toast.makeText(this, "Email is null", Toast.LENGTH_SHORT).show();
            }else if (username.isEmpty()){
                Toast.makeText(this, "Username is null", Toast.LENGTH_SHORT).show();
            }else if (pass.isEmpty()){
                Toast.makeText(this, "Password is null", Toast.LENGTH_SHORT).show();
            }else if (confirmPass.isEmpty()){
                Toast.makeText(this, "Confirm password is null", Toast.LENGTH_SHORT).show();
            }else {
                if (pass.equals(confirmPass)){
                    binding.signUpBtn.setEnabled(false);
                    binding.progressCircular.setVisibility(View.VISIBLE);
                    createUser(email, pass);
                }else {
                    Toast.makeText(this, "Password and confirm password not same", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createUser(String email, String pass){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult ->
                        Objects.requireNonNull(auth.getCurrentUser()).sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        saveAccount(Objects.requireNonNull(authResult.getUser()).getUid());
                                    }else {
                                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        binding.signUpBtn.setEnabled(true);
                                        binding.progressCircular.setVisibility(View.GONE);
                                    }
                }))
                .addOnFailureListener(e -> {
                    binding.signUpBtn.setEnabled(true);
                    binding.progressCircular.setVisibility(View.GONE);
                    Toast.makeText(SignUp.this, "Sign up failed : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Error", e.getMessage());
        });
    }

    // save user data in database
    private void saveAccount(String uid){
        String username = binding.usernameSignUp.getText().toString();
        String email = binding.emailSignUp.getText().toString();

        // status account (true == active, false == non active)
        ModelAccount modelAccount = new ModelAccount(
           username,
           email,
           true
        );

        Variables.referenceRoot.child("users").child(uid).setValue(modelAccount)
                .addOnSuccessListener(unused -> {
                    binding.signUpBtn.setEnabled(true);
                    binding.progressCircular.setVisibility(View.GONE);
                    confirmSignUp();
                })
                .addOnFailureListener(e -> {
                    binding.signUpBtn.setEnabled(true);
                    binding.progressCircular.setVisibility(View.GONE);
                    Toast.makeText(SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void confirmSignUp(){
    //Create the Dialog here
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_confirmation_sign_up);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_center_dialog));

        dialog.getWindow().setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        dialog.setCancelable(false); //Optional
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation; //Setting the animations to dialog

        Button Okay = dialog.findViewById(R.id.btn_okay);

        dialog.show();
        Okay.setOnClickListener(v -> {
            Functions.updateUI(SignUp.this, SignIn.class);
            dialog.dismiss();
        });
    }
}