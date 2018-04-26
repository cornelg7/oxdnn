package oxfordteam5.neuralnetwork;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SleepReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        final String action = intent.getAction();
        if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
            intent.setClass(context, SleepService.class);
            context.startService(intent);
            context.sendBroadcast(intent);
            Log.e("Receiver", "receiver was called and an intent broadcasted");
        }
    }

}
