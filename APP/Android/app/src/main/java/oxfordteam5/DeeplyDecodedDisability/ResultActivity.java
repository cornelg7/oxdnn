package oxfordteam5.DeeplyDecodedDisability;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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
    TextToSpeech voice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        message = findViewById(R.id.textView2);
        view = findViewById(R.id.imageView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent start = getIntent();
        String path = start.getStringExtra("path");
        String name = start.getStringExtra("name");

        //message.setText(path);
        util = new Utilities(this);;
        util.upload(message,view,path,name, null);
    }


    @Override
    public void onDestroy () {
        super.onDestroy();
    }
}
