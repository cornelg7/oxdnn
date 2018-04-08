package oxfordteam5.neuralnetwork;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OptionsActivity extends AppCompatActivity {

    boolean saveImg;
    boolean privateImg;

    File output;
    TextView message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Intent start = getIntent();
        saveImg = start.getBooleanExtra("saveImages", false);
        ( (CheckBox) findViewById(R.id.checkBoxSave) ).setChecked(!saveImg);
        privateImg = start.getBooleanExtra("privateImages", true);
        ( (CheckBox) findViewById(R.id.checkBoxPrivate) ).setChecked(!privateImg);

        message = (TextView) findViewById(R.id.textView5);
        message.setVisibility(View.INVISIBLE);

        output = new File(getExternalFilesDir(null), "temp_options.txt");
    }


    public void ChangeSave(View view) {

        //linked to checkbox for "Delete pictures after use"
        saveImg =  ! ((CheckBox) view).isChecked();

        if(!saveImg) {
            privateImg = true;
            ( (CheckBox) findViewById(R.id.checkBoxPrivate) ).setChecked(false); //so we can't save picture to gallery
        }

        try {
            writeOutput(saveImg,privateImg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //message.setText("changeSave called");

    }

    public void ChangePrivate(View view) {

        //linked to checkbox "save picture to gallery"
        privateImg =  !((CheckBox) view).isChecked();

        if( !privateImg) {
            saveImg = true;
            ( (CheckBox) findViewById(R.id.checkBoxSave) ).setChecked(false); //so we can't delete pictures if they are public
        }

        try {
            writeOutput(saveImg,privateImg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //message.setText("changePrivate called");
    }

    private void writeOutput (Boolean save, Boolean pri) throws IOException {

        FileOutputStream stream = new FileOutputStream(output);
        stream.write((save.toString()+" "+pri.toString()).getBytes());
        stream.close();
    }

}
