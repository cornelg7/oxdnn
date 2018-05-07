package oxfordteam5.DeeplyDecodedDisability;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    static TextView message = null;
    static ImageView view = null;
    Utilities util;
    //TextToSpeech voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        message = findViewById(R.id.textView2);
        view = findViewById(R.id.imageView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent start = getIntent();
        final String path = start.getStringExtra("path");
        final String name = start.getStringExtra("name");

        //message.setText(path);
        util = new Utilities(this);

        util.upload(message,view,path,name, null);

        /* TTS test code
        voice = new TextToSpeech(this,new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                setLanguage();
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                util.upload(message,view,path,name, voice);
            }
        }, 5000);   //5 seconds*/

    }

   /* TTS test code
   protected void  setLanguage() {
        voice.setLanguage(Locale.ENGLISH);
    }*/

    @Override
    public void onDestroy () {
        //voice.shutdown();
        super.onDestroy();
    }

}
