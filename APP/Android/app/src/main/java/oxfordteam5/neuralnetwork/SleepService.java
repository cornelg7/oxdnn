package oxfordteam5.neuralnetwork;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class SleepService extends IntentService {


    public SleepService() {
        super("SleepService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("intent Service", "handling stuff");
        if (intent != null) {
            final String action = intent.getAction();
            if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) return; //quit if there is no event

                int code = event.getKeyCode();

                if ((code == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || code == KeyEvent.KEYCODE_HEADSETHOOK) && event.getAction() == KeyEvent.ACTION_DOWN) {

                    //DO SOMETHING
                    Log.e("intent service", "intent service started!!!");

                }
            }
        }
    }

}
