package com.example.getmenu;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SelectPicture extends AppCompatActivity{

    private Button shots;
    private Button loads;
    private Button test;
    private ImageView imageView;
    private String imageFilePath;
    private String encodedImage;  // 인코딩된 이미지 파일
    private Uri photoUri;

    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_LOAD = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectpicture_main);

        imageView = findViewById(R.id.imageView);
        shots = findViewById(R.id.shot);
        loads = findViewById(R.id.load);
        test = findViewById(R.id.server_test);
        shots.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){ takePhoto(); }
        });

        loads.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){ selectPhoto(); }
        });

        test.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //String sMessage = "abctest"; // 보내는 메시지를 받아옴
                //String result = SendByHttp(sMessage); // 메시지를 서버에 보냄
                //String[][] parsedData = jsonParserList(); // 받은 메시지를 json 파싱
                //tvRecvData.setText(result); // 받은 메시지를 화면에 보여주기
            }
        });
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, REQUEST_IMAGE_LOAD);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Menu_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();

        return image;
    }

    public static Bitmap rotatedImage(Bitmap bitmap, float angle){
        Matrix a = new Matrix();
        a.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), a, true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // 사진 카메라로 찍기
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

            //Bitmap transfer = null;
            //transfer.createScaledBitmap(bitmap,256,256,true);
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //transfer.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            //String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            try {
                exif = new ExifInterface(imageFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orient = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap rotated = null;
            switch (orient){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotated = rotatedImage(bitmap,90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotated = rotatedImage(bitmap,180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotated = rotatedImage(bitmap,270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotated = bitmap;

            }

            imageView.setImageBitmap(rotated);

            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            rotated.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        }

        if (requestCode == REQUEST_IMAGE_LOAD && resultCode == RESULT_OK) { // 사진 갤러리에서 불러오기
            Uri fileUri = data.getData();
            ContentResolver resolver = getContentResolver();
            try {
                InputStream instream = resolver.openInputStream(fileUri);
                Bitmap imgBitmap = BitmapFactory.decodeStream(instream);
                imageView.setImageBitmap(imgBitmap);    // 선택한 이미지 이미지뷰에 셋

                ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
                imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
                byte[] b = byteArrayBitmapStream.toByteArray();
                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);

                instream.close();
                Toast.makeText(getApplicationContext(), "파일 불러오기 성공", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "파일 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
