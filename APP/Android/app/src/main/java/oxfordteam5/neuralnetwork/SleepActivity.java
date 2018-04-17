package oxfordteam5.neuralnetwork;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SleepActivity extends AppCompatActivity {

    TextView message;
    Camera myCamera;
    Boolean saveImages;
    Boolean privateImages;
    File lastPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        message = findViewById(R.id.textView6);
        message.setText("You can now lock the the screen");
        myCamera = null;

        Intent start = getIntent();
        saveImages = start.getBooleanExtra("save",true);
        privateImages = start.getBooleanExtra("private", false);

        lastPicture = null;
        askPermissions(112);

        Intent intent = new Intent(this, SleepService.class);
        intent.putExtra("save",saveImages);
        intent.putExtra("private",privateImages);
        startService(intent);

    }


    public void TakePicture(View view) {
        if(!saveImages && lastPicture != null) {
            if(lastPicture.exists()) lastPicture.delete();
            lastPicture= null;
        }

        if(myCamera != null) {
            myCamera.release();
            myCamera = null;
        }

        try {
            myCamera = Camera.open();
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            return;
        }

        try {
            myCamera.takePicture(null, null, myPicture);
        } catch (Exception e) {
            Log.e(getString(R.string.app_name), "failed to open Camera");
            return;
        }

    }

    private Camera.PictureCallback myPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File picture;
            File storageDir;
            if(privateImages) storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            else storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            try {
                picture = File.createTempFile("IMG", ".jpg", storageDir);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(getString(R.string.app_name), "failed to create file for picture taken");
                return;
            }

            lastPicture = picture; //lastPicture was deleted before taking the picture

            try {
                FileOutputStream output = new FileOutputStream(picture);
                output.write(bytes);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(getString(R.string.app_name), "failed to write data to the picture file");
                return;
            }

            if(!privateImages) { //update gallery to add image
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(picture);
                mediaScanIntent.setData(contentUri);
                SleepActivity.this.sendBroadcast(mediaScanIntent);
            }
            //here we send the file to the server
        }
    };

    private boolean askPermissions(int code) {
        if (ContextCompat.checkSelfPermission(SleepActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SleepActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    code);

            return false;

        }
        if (ContextCompat.checkSelfPermission(SleepActivity.this,
                Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SleepActivity.this,
                    new String[]{Manifest.permission.WAKE_LOCK},
                    code);

            return false;

        }
        if (ContextCompat.checkSelfPermission(SleepActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SleepActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);

            return false;

        }
        if (ContextCompat.checkSelfPermission(SleepActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(SleepActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    code);

            return false;

        }
        return  true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if( askPermissions(113)) return;
        }
        else {
            message.setText("app cannot run without permission; \n Closing ina few seconds");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 5000);
        }
    }

    public void onDestroy() { //when the app is getting killed

        if(!saveImages && lastPicture != null) { //delete images; images are private
            if (lastPicture.exists()) lastPicture.delete();
            lastPicture = null;
        }

        super.onDestroy();
    }
}
