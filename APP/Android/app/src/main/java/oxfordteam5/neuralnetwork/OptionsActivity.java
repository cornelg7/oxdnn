package oxfordteam5.neuralnetwork;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class OptionsActivity extends AppCompatActivity {

    /*
     * THE CONTENT OF THE WRITEOUTPUT FUNCTION SHOULD BE THE SAME AS THE WRITEOPTIONS FUNCTION IN THE MAIN ACTIVITY
     *
     * THE ONLY DIFFERENCE IS THE THIS ACTIVITY WRITES IN THE TEMP FILE AND NOT THE PERMANENT ONE
     *
     * THE SECOND WRITEOUTPUT FUNCTION SHOULD BEHAVE THE SAME AS IN MAIN ACTIVITY
     *
     */

    boolean saveImg;
    boolean rotateImg;
    boolean focus;

    File output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent start = getIntent();
        saveImg = start.getBooleanExtra("saveImages", false);
        ( (CheckBox) findViewById(R.id.checkBoxSave) ).setChecked(saveImg);
        rotateImg = start.getBooleanExtra("rotateBitmap", true);
        ( (CheckBox) findViewById(R.id.checkBoxRotateBitmap) ).setChecked(rotateImg);
        focus = start.getBooleanExtra("focusCamera", true);
        ( (CheckBox) findViewById(R.id.checkBoxFocusCamera) ).setChecked(focus);

        output = new File(getExternalFilesDir(null), "temp_options.txt");
        if(!output.exists()) {
            try {
                output.createNewFile();
            } catch (IOException e) {
                Log.e("NueralNetwork", "cannot create temp file which is missing");
                e.printStackTrace();
            }

        }
    }

    //CHANGE THE VALUE OF SAVEIMG = DOES THE USER WANT THE IMAGES TO BE SAVED?
    public void ChangeSave(View view) {

        //linked to checkbox for "Save images in the gallery"
        saveImg =  ((CheckBox) view).isChecked();

        try {
            writeOutput(saveImg, rotateImg, focus);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //CHANGE THE VALUE OF ROTATEIMG = DOES THE USER WANT THE IMAGE TO FIT THE SCREEN?
    public void  ChangeRotate (View view) {

        rotateImg = ((CheckBox) view).isChecked();

        try {
            writeOutput("rotateBitmap", rotateImg);
        } catch (IOException e) {
            Log.e("NeuralNetwork", "cannot write to options temp file");
            e.printStackTrace();
        }
    }

    //CHANGE THE VALUE OF FOCUS = DOES THE USER WANT THE CAMERA TO FOCUS BEFORE TAKING PICTURE
    public void  ChangeFocus (View view) {

        focus = ((CheckBox) view).isChecked();

        try {
            writeOutput("focusCamera", focus);
        } catch (IOException e) {
            Log.e("NeuralNetwork", "cannot write to options temp file");
            e.printStackTrace();
        }
    }

    //WRITE THE OPTIONS PARAMETERS IN THE FILE
    private void writeOutput (Boolean save, Boolean rot, Boolean foc) throws  IOException {

        //get file and output stream
        FileOutputStream stream = new FileOutputStream(output);
        //write data and close
        stream.write((save.toString()+ "\n"+ rot.toString() + "\n"+foc.toString()).getBytes());
        stream.close();

    }

    //OVERLOADING TO ALLOW MORE FLEXIBILITY
    private void writeOutput (String name , Boolean value) throws  IOException {
        switch (name) {
            case "saveImages":
                writeOutput(value,rotateImg,focus);
                return;
            case "rotateBitmap":
                writeOutput(saveImg,value,focus);
                return;
            case "focusCamera":
                writeOutput(saveImg,rotateImg,value);
                return;
            default:
                return;
        }
    }

}
