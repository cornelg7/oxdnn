package oxfordteam5.neuralnetwork;

import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class SleepService extends Service {

    MediaSession _mediaSession;
    MediaSession.Token _mediaSessionToken;
    final String TAG = "NeuralNetwork";
    Boolean focusCamera = null, saveImages = null;
    Utilities util;
    TextToSpeech voice;

    public SleepService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {

        util = new Utilities(this);

        _mediaSession = new MediaSession(getApplicationContext(), TAG);

        _mediaSessionToken = _mediaSession.getSessionToken();

        _mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();

        _mediaSession.setPlaybackState(state);

        _mediaSession.setActive(true);


        voice = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                setLanguage();
            }
        });

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        focusCamera = intent.getBooleanExtra("focus", true);
        saveImages = intent.getBooleanExtra("save", true);

        _mediaSession.setCallback(new MediaSession.Callback() {

            public void onPlay() {
                Log.e(TAG, "onPlay called (media button pressed)");

                util.takePictureAndUpload(null, focusCamera,saveImages, null,voice);

                super.onPlay();
            }

        });


        return super.onStartCommand(intent,flags,startId);
    }

    private void setLanguage() {
        voice.setLanguage(Locale.ENGLISH);
    }

    @Override
    public void onDestroy () {
        voice.shutdown();
        super.onDestroy();
    }
}
