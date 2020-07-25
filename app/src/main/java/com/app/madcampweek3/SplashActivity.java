package com.app.madcampweek3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;


public class SplashActivity extends Activity {
    ImageView imageView;            // Logo ImageView
    Animation imageAni;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imageView = (ImageView) findViewById(R.id.splash_logo);
        imageAni = AnimationUtils.loadAnimation(this, R.anim.anim_splash_imageview);
        imageView.startAnimation(imageAni);

        imageAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }
            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

    }
}