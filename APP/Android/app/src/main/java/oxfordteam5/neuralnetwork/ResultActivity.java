package oxfordteam5.neuralnetwork;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ResultActivity extends AppCompatActivity {

    static TextView message = null;
    Utilities util;
    TextToSpeech voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        message = findViewById(R.id.textView2);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent start = getIntent();
        String path = start.getStringExtra("path");
        String name = start.getStringExtra("name");

        message.setText(path);
        util = new Utilities(this);;
        util.upload(message,path,name, null);

         voice = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

            }
        });
    }


    public void Speak (View view) {
        voice.setLanguage(Locale.ENGLISH);
        voice.speak(message.getText(),TextToSpeech.QUEUE_FLUSH,null,"Trial");

    }


    @Override
    public void onDestroy () {
        voice.shutdown();
        super.onDestroy();
    }
}
