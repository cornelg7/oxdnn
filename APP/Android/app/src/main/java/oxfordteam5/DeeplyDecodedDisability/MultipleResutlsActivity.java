package oxfordteam5.DeeplyDecodedDisability;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

public class MultipleResutlsActivity extends AppCompatActivity {

    final List<Pair<String,String>> FileList = new LinkedList<Pair<String,String>>();

    private TextView message;
    private LinearLayout parent;
    final String TAG = "DeeplyDecodedDisability";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_resutls);

        message = (TextView) findViewById(R.id.textView3);
        parent = (LinearLayout) findViewById(R.id.container);

        Intent start = getIntent();
        int size = start.getIntExtra("filesNumber", -1);
        for (int i =0; i<size; i++) { //get all list, item by item
            String path = start.getStringExtra("path"+i);
            String name = start.getStringExtra("name"+i);
            Log.e(TAG,"path "+path);
            FileList.add(new Pair<String, String>(path, name));
        }

        UploadAndDelete();
        Log.e("DDD", "in multipleUploads");
    }

    private void UploadAndDelete() {//upload and delete all files from the list

        message.setVisibility(View.VISIBLE);
        message.setText("Analysing the pictures");
        for (int i=0; i<FileList.size(); i++) {
            final String path = FileList.get(i).getLeft();
            final String name = FileList.get(i).getRight();
            ZoomageView img = getImage(i);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,16,0,16);
            //img.setScaleType(CENTER_INSIDE);
            //img.setAdjustViewBounds(true);;

            if(img != null) {
                img.setLayoutParams(lp);
                new Utilities(this).upload2(message,img, path,name,null,true);
            }
        }


    }


    private ZoomageView getImage(int i) {


        switch (i) {
            case 0:
                return findViewById(R.id.image0);
            case 1:
                return findViewById(R.id.image1);
            case 2:
                return findViewById(R.id.image2);
            case 3:
                return findViewById(R.id.image3);
            case 4:
                return findViewById(R.id.image4);
            case 5:
                return findViewById(R.id.image5);
            case 6:
                return findViewById(R.id.image6);
            case 7:
                return findViewById(R.id.image7);
            case 8:
                return findViewById(R.id.image8);
            case 9:
                return findViewById(R.id.image9);
            case 10:
                return findViewById(R.id.image10);
            case 11:
                return findViewById(R.id.image11);
            case 12:
                return findViewById(R.id.image12);
            case 13:
                return findViewById(R.id.image13);
            case 14:
                return findViewById(R.id.image14);
            case 15:
                return findViewById(R.id.image15);
            case 16:
                return findViewById(R.id.image16);
            case 17:
                return findViewById(R.id.image17);
            case 18:
                return findViewById(R.id.image18);
            case 19:
                return findViewById(R.id.image19);
            case 20:
                return findViewById(R.id.image20);

        }

        return null;
    }
}
