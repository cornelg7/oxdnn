package oxfordteam5.neuralnetwork;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);
        message = findViewById(R.id.textView6);
        message.setText("You can now lock the the screen");
        myCamera = null;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent start = getIntent();
        saveImages = start.getBooleanExtra("save",true);
        privateImages = start.getBooleanExtra("private", false);

        askPermissions(112);

        Intent intent = new Intent(this, SleepService.class);
        intent.putExtra("save",saveImages);
        intent.putExtra("private",privateImages);
        startService(intent);

    }

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
            message.setText("app cannot run without permission; \n Closing in a few seconds");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 5000);
        }
    }

    @Override
    public void onDestroy() { //when the app is getting killed

        Intent intent = new Intent(this, SleepService.class);
        stopService(intent);
        super.onDestroy();
    }
}
