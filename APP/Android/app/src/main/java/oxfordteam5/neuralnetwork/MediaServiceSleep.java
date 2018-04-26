package oxfordteam5.neuralnetwork;


import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;

public class MediaServiceSleep extends MediaBrowserServiceCompat {

    private static final String MY_MEDIA_ROOT_ID = "SleepServiceNeuralNetwork";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "EmptySleepServiceNeuralNetwork";
    private static final int MEDIA_SERVICE_ID = 21;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackBuilder;
    private MySessionCallback mediaCallback;

    @Override
    public void onCreate() {
        super.onCreate();

        //CREATE MEDIASESSION AND PLAYBACKSTATE

        mediaSession = new MediaSessionCompat(this, "NeuralNetworkSleepService");
        // Enable callbacks from MediaButtons and TransportControls

        mediaCallback = new MySessionCallback();

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(mediaCallback);

        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        playbackBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mediaSession.setPlaybackState(playbackBuilder.build());


        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());

        //CREATE NOTIFICATION BAR

        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this);
        notifBuilder.setContentTitle("NeuralNetwork Sleep Service")
                .setContentText("Sleep mode for NeuralNetwork")
                .setSubText("This allow you to take picture and analyse them while the phone is locked");
                //maybe we should add an icon

        MediaControllerCompat controller = mediaSession.getController();
        notifBuilder.setContentIntent(controller.getSessionActivity())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notifBuilder.setStyle(new android.support.v7.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP)));
        startForeground(MEDIA_SERVICE_ID, notifBuilder.build());

        Log.i("NeuralNetwork", "service started");
    }

    private class MySessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            Log.e("NeuralNetwork","service working");
            //take picture; send to server and read response
        }

        @Override
        public  void onPause() {
            //execute onPlay
        }

        @Override
        public  void onStop() {
            //stop service
            Intent intent = new Intent(MediaServiceSleep.this, MediaServiceSleep.class);
            stopService(intent);
        }
    }




    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null); //any app can connect
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null); //no app can browser any mediaItem (there should be none)
        return;
    }
}
