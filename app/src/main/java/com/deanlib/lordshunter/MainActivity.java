package com.deanlib.lordshunter;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
//        String mail1 = intent.getStringExtra(Intent.EXTRA_EMAIL);
//        String mail2 = intent.getStringExtra(Intent.EXTRA_CC);
        String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        ArrayList<Uri> parcelableArrayListExtra = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

//        System.out.println("MAMAMAMAMAMAMAAMAMAM mail1:"+mail1);
//        System.out.println("MAMAMAMAMAMAMAAMAMAM mail2:"+mail2);
        System.out.println("MAMAMAMAMAMAMAAMAMAM subject:"+subject);
        System.out.println("MAMAMAMAMAMAMAAMAMAM text:"+text);
        System.out.println("MAMAMAMAMAMAMAAMAMAM uri:"+parcelableArrayListExtra);

    }
}
