package com.example.lottieanimation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    LottieAnimationView lottieView1, lottieView2;
    Button playButton1, playButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lottieView1 = findViewById(R.id.lottieView1);
        lottieView2 = findViewById(R.id.lottieView2);
        playButton1 = findViewById(R.id.playButton1);
        playButton2 = findViewById(R.id.playButton2);

        playButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lottieView1.playAnimation();
            }
        });

        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lottieView2.playAnimation();
            }
        });
    }
}