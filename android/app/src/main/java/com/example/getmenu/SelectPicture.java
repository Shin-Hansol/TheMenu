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
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SelectPicture extends AppCompatActivity{

    private Button shots;
    private Button loads;
    private Button test;
    private ImageView imageView;
    private String imageFilePath;
    private String encodedImage;  // 인코딩된 이미지 파일
    private Uri photoUri;
    private Retrofit retrofit;
    private ApiService service;

    private File photoFile = null;


    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final int REQUEST_IMAGE_LOAD = 101;
    private final String URL = "http://10.0.2.2:3000/";
    private final String TAG = "TestLog";

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
                sendImage();
            }
        });
    }

    private void sendImage(){
        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(ApiService.class);

        RequestBody reqFile = RequestBody.create(photoFile, MediaType.parse("image/*"));
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", photoFile.getName(), reqFile);
        RequestBody name = RequestBody.create("upload", MediaType.parse("text/plain"));

        Intent intent = getIntent();
        String src = intent.getExtras().getString("src");
        String dst = intent.getExtras().getString("dst");

        Call<ResponseBody> call_post = service.postImage(body, name, src, dst);
        call_post.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String result = response.body().string();
                        Log.v(TAG, "result = " + result);
                        Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.v(TAG, "error = " + String.valueOf(response.code()));
                    Toast.makeText(getApplicationContext(), "error = " + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.v(TAG, "Fail");
                Toast.makeText(getApplicationContext(), "Response Fail", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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

    public void saveBitmapToJpg(Bitmap bitmap , String name) {
        File storage = getCacheDir(); //  path = /data/user/0/YOUR_PACKAGE_NAME/cache
        String fileName = name + ".jpg";
        File imgFile = new File(storage, fileName);
        try {
            imgFile.createNewFile();
            FileOutputStream out = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); //썸네일로 사용하므로 퀄리티를 낮게설정
            out.close();
        } catch (FileNotFoundException e) {
            Log.e("saveBitmapToJpg","FileNotFoundException : " + e.getMessage());
        } catch (IOException e) {
            Log.e("saveBitmapToJpg","IOException : " + e.getMessage());
        }
        Log.d("imgPath" , getCacheDir() + "/" +fileName);
        photoFile = imgFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { // 사진 카메라로 찍기
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);
            ExifInterface exif = null;

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

                saveBitmapToJpg(imgBitmap, "temp");

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
