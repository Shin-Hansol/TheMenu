package com.example.getmenu;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        Button imageButton = (Button) findViewById(R.id.btn1);
        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelectPicture.class);
                startActivity(intent);
            }
        });
    }

    public void checkPermission(){
        int permissionCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permissionCamera != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
        }
        if(permissionWrite != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if(permissionRead != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    //@Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(this, "권한이 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }
}