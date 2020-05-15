package com.example.smstts;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class helpActivity extends AppCompatActivity{

    private ImageButton back2setting;
    private TextView helptext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helptext = (TextView)findViewById(R.id.helptextview);
        helptext.setMovementMethod(new ScrollingMovementMethod());
        back2setting = (ImageButton) findViewById(R.id.back2settingBtn);
        back2setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), setDisplay.class);
                startActivity(intent);
            }
        });

    }
}
