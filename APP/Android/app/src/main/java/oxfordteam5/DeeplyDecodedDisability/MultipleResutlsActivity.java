package oxfordteam5.DeeplyDecodedDisability;

import android.content.Intent;
import android.graphics.Canvas;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

public class MultipleResutlsActivity extends AppCompatActivity {

    final List<Pair<String,String>> FileList = new LinkedList<Pair<String,String>>();

    private TextView message;
    private LinearLayout container;
    final String TAG = "DeeplyDecodedDisability";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_resutls);

        message = (TextView) findViewById(R.id.textView3);
        container = (LinearLayout) findViewById(R.id.container);

        Intent start = getIntent();
        int size = start.getIntExtra("filesNumber", -1);
        for (int i =0; i<size; i++) { //get all list, item by item
            String path = start.getStringExtra("path"+i);
            String name = start.getStringExtra("name"+i);
            Log.e(TAG,"path "+path);
            FileList.add(new Pair<String, String>(path, name));
        }

        UploadAndDelete();
    }

    private void UploadAndDelete() {//upload and delete all files from the list

        for (int i=0; i<FileList.size(); i++) {
            final String path = FileList.get(i).getLeft();
            final String name = FileList.get(i).getRight();
            ImageView img = new android.support.v7.widget.AppCompatImageView(this) {
                @Override
                protected void  onDraw(Canvas canvas) {
                    super.onDraw(canvas); //draw everything
                    File image = new File(path);
                    if(image.exists()) image.delete();
                }
            };
            DrawerLayout.LayoutParams lp = new DrawerLayout.LayoutParams(DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0,16,0,16);
            img.setLayoutParams(lp);
            container.addView(img);
            new Utilities(this).upload(message,img, path,name,null,true);

        }


    }
}
