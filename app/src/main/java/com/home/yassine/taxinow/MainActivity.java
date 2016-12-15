package com.home.yassine.taxinow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton mSignInBtn;
    private AppCompatButton mSignUpBtn;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        User u = Storage.LoadUser(this);

        if (u != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        getSupportActionBar().hide();

        mSignUpBtn = (AppCompatButton) findViewById(R.id.sign_up_btn);
        mSignInBtn = (AppCompatButton) findViewById(R.id.sign_in_btn);

        final Context context = this;

        mSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });

        mSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}
