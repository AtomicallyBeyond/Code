package com.example.kidzcolor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.PathParser;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.kidzcolor.models.VectorMasterDrawable;
import com.example.kidzcolor.models.VectorModel;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        VectorModel vectorModel = new VectorModel(this, "ic_school.xml");
        VectorMasterDrawable vectorMasterDrawable = new VectorMasterDrawable(vectorModel);

        Drawable drawable = getResources().getDrawable(R.drawable.ic_school);


        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageDrawable(vectorMasterDrawable);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent coloringIntent = new Intent(MainActivity.this, ColoringActivity.class);
                MainActivity.this.startActivity(coloringIntent);
            }
        });*/
    }
}