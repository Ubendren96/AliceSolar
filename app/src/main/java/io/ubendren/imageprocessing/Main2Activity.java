package io.ubendren.imageprocessing;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainintent = new Intent(Main2Activity.this,LoginActivity.class);
                startActivity(mainintent);
                finish();
            }
        },1000);
    }
}
