package com.example.kkenneally.ocr_scan_img;
//original version
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String TESS_DATA = "/tessdata";
    private TextView textView;
    private TessBaseAPI tessBaseAPI;
    private Uri outputFileDir;
    private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString()+"/Tess";
   // private static final String DATA_PATH = Environment.getExternalStorageDirectory().toString();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) this.findViewById(R.id.id_text_view);
        this.findViewById(R.id.id_scan_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startCamera();
            }
        });


    }

    private void startCamera(){
        try{
           // String pathToImage = DATA_PATH+"/imgs";
            String pathToImage = DATA_PATH;

            File dir = new File(pathToImage);
            if(!dir.exists()){
                dir.mkdir();
                Log.e(TAG, "CREATED DIRECTORY");

            }

            Log.e(TAG, "DIRECTORY EXISTS........................");
            Log.e(TAG, dir.getAbsolutePath());


            String pathToImageFile = pathToImage+"/ocr.jpg";
          //  String pathToImageFile = DATA_PATH;

            outputFileDir = Uri.fromFile(new File(pathToImageFile));
            final Intent intentPic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentPic.putExtra(MediaStore.EXTRA_OUTPUT, outputFileDir);
            if(intentPic.resolveActivity(getPackageManager()) != null){
                startActivityForResult(intentPic, 100);
            }
        }catch(Exception e){
            Log.e(TAG, e.getMessage());
            Log.e(TAG, "FAILURE AT start camera");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            prepareTessData();

            Log.e(TAG, "output fle directory");
            Log.e(TAG, outputFileDir.getPath().toString());
            startOCR(outputFileDir);



        }else{
            Toast.makeText(getApplicationContext(), "Image problem", Toast.LENGTH_SHORT).show();
        }

    }

    private void prepareTessData(){
        try{
            //File dir = new File(DATA_PATH + TESS_DATA);
            File dir = getExternalFilesDir(TESS_DATA);

            if(!dir.exists()){
                dir.mkdir();
                Log.e(TAG, "DIRECTORY CREATED AT LOCATION: ");
                Log.e(TAG, dir.getAbsolutePath());

            }

            Log.e(TAG, "DIRECTORY FOR TESSDATA EXISTS AT: ");
            Log.e(TAG, dir.getAbsolutePath());



            String fileList[] = getAssets().list("");
            for(String fileName : fileList){
              //  String pathToDataFile = DATA_PATH + TESS_DATA+"/"+fileName;
               // String pathToDataFile = DATA_PATH + TESS_DATA+"/"+fileName;
                String pathToDataFile = dir + "/" + fileName;


                Log.e(TAG, "pathToDatfile is :");

                Log.e(TAG, pathToDataFile);

                InputStream in = getAssets().open(fileName);
                OutputStream out = new FileOutputStream(pathToDataFile);
                byte [] buff = new byte[1024];
                int len;
                while (( len = in.read(buff) ) > 0){
                    out.write(buff, 0, len);
                }
                in.close();
                out.close();
            }
        }catch (IOException e){
            Log.e(TAG, e.getMessage());
            Log.e(TAG, "Failed at prepareTessData()");

        }
    }

    private void startOCR(Uri imageUri){
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 7;
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath(), options);
            String result = this.getText(bitmap);
            textView.setText(result);


            Log.e(TAG, "the image url is ");
            Log.e(TAG, imageUri.getPath());
            Log.e(TAG, "the result is ");
            Log.e(TAG, result
            );
        }catch(Exception e){
            Log.e(TAG, "Failure at startURI()");
        }
    }

    private String getText(Bitmap bitmap){
        try{
            tessBaseAPI = new TessBaseAPI();
        }catch (Exception e){
            Log.e(TAG, "FAILURE AT GET TEXT");
        }
        tessBaseAPI.init(DATA_PATH, "eng");
        //tessBaseAPI.init(DATA_PATH, "dig");



        //       //EXTRA SETTINGS
//        //For example if we only want to detect numbers
  //      tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890");
//
//        //blackList Example
//        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
//                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");



        tessBaseAPI.setImage(bitmap);
        tessBaseAPI.getThresholdedImage();
        //bitmap.eraseColor(212);
        String res = "No result";
        try{
          //  res = "Nothing Found";
            res= tessBaseAPI.getUTF8Text();
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            Log.e(TAG, "FAILURE AT GET TEXT nothing found");
        }
        tessBaseAPI.end();
        return res;
    }
}
