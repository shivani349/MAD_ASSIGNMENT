package com.example.sign_in;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    TextView welcomeText;
    Button logoutButton;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        welcomeText = findViewById(R.id.welcome_text);
        logoutButton = findViewById(R.id.logout_button);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            welcomeText.setText("Welcome, " + user.getDisplayName());
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });
    }
}
