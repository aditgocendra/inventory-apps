package com.ark.inventory_apps.View;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import com.ark.inventory_apps.Globals.Functions;
import com.ark.inventory_apps.Globals.Variables;
import com.ark.inventory_apps.R;
import com.ark.inventory_apps.View.Auth.Authentication;
import com.ark.inventory_apps.View.Users.Home;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Functions.checkWindowSetFlag(this);

        Handler handler = new Handler();
        handler.postDelayed(this::routes, 1000);
    }

    private void routes(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            Functions.updateUI(this, Home.class);
            Variables.uid = user.getUid();
        }else {
            Functions.updateUI(this, Authentication.class);
        }
        finish();

    }
}