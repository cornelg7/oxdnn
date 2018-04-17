package oxfordteam5.neuralnetwork;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;




/*
    I need to make an audio service, so that I can hadle the media buttons
    https://developer.android.com/guide/topics/media-apps/mediabuttons.html
    https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app.html
    https://developer.android.com/guide/topics/media-apps/audio-app/building-a-mediabrowserservice.html
    The above is the link to the android guide. I should give a look at "build an audio app"
    Moreover to have android to read out loud some text I should use TTS (Text To Speach)
    https://developer.android.com/reference/android/speech/tts/TextToSpeech.html
 */
public class SleepService extends Service {
    public SleepService() {
    }

    Boolean saveImages;
    Boolean privateImages;
    static final String CHANNEL_ID = "sleepService";
    static Notification notification = null;
    static final String MEDIA_TAG = "NeuralNetworkMedia";
    static MediaSessionCompat mediaSession = null;
    static MediaControllerCompat mediaController = null;
    static PlaybackStateCompat playback = null;
    static MediaSessionCompat.Callback myCallback = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {


        //build notification for the user tab
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID );
        builder.setContentTitle("MyApp");
        builder.setContentText("This let you take instant photo");
        builder.setPriority(NotificationCompat.PRIORITY_LOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        notification = builder.build();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(2121,notification); //this must be called within 5 second from the start of the service

        if (mediaSession == null) mediaSession = new MediaSessionCompat(this,MEDIA_TAG);
        if (mediaController == null) mediaController = new MediaControllerCompat(this, mediaSession);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS);
        PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder();
        builder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_STOP);
        builder.setState(PlaybackStateCompat.STATE_STOPPED,0,1);
        playback = builder.build();
        mediaSession.setPlaybackState(playback);
        if(myCallback == null) myCallback= new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {};

            @Override
            public  void onPause() {};

            @Override
            public void onStop() {};

        };
        mediaSession.setCallback(myCallback);

        saveImages = intent.getBooleanExtra("save", true);
        privateImages = intent.getBooleanExtra("private",false);

        return START_STICKY;
    }




    //stopSelf(); must be call to close the service
    //meidaSession.release(); //to end the media session

    @Override
    public void onDestroy() {

    }

}
