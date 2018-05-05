package oxfordteam5.DeeplyDecodedDisability;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;


import java.io.File;
import java.util.Locale;

public class SleepService extends Service {

    MediaSession mediaSession;
    MediaSession.Token mediaToken;
    final String TAG = "NeuralNetwork";
    Boolean focusCamera = null, saveImages = null;
    Utilities util;
    TextToSpeech voice;
    File Image;

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
        Image = null;

        mediaSession = new MediaSession(getApplicationContext(), TAG);

        //mediaToken = mediaSession.getSessionToken();

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackState playState = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0)
                .build();

        mediaSession.setPlaybackState(playState);

        mediaSession.setActive(true);


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

        mediaSession.setCallback(new MediaSession.Callback() {

            public void onPlay() {
                Log.e(TAG, "onPlay called (media button pressed)");
                Image = util.Image;

                util.takePictureAndUpload(Image, focusCamera,saveImages,true,null,null,voice); //should not be false

                super.onPlay();
            }

        });

        Boolean close = intent.getBooleanExtra("closeService", false);

        if(close) stopSelf();
        createNotification();

        return super.onStartCommand(intent,flags,startId);
    }

    private void setLanguage() {
        voice.setLanguage(Locale.ENGLISH);
    }

    @Override
    public void onDestroy () {
        Log.e(TAG,"onDestory in SleepService was called");
        voice.shutdown();
        NotificationManagerCompat.from(this).cancelAll();
        stopSelf();
        mediaSession.release();
        super.onDestroy();
    }

    private void createNotification () {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "SleepService");
        builder.setSmallIcon(R.drawable.gearn4)
                .setContentTitle("Sleep Service for NeuralNetwork")
                .setContentText("Use your headphones to take pictures and analyse them")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Use your headphones to take pictures and analyse them"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pendingIntent);

        Intent closeIntent = new Intent(this, SleepService.class);
        closeIntent.putExtra("closeService", true);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0, closeIntent, 0);

        builder.addAction(0, "close service",
                closePendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //if running on API 26 or higher (Android Oreo 8.0)

            CharSequence name = "SleepServiceChannel";
            String description ="SleepService for NeuralNetwork";
            NotificationChannel channel = new NotificationChannel("SleepService", name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat.from(this).notify(2121, builder.build());
    }

}
