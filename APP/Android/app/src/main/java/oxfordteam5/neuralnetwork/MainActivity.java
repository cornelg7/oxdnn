package oxfordteam5.neuralnetwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int LOAD_IMAGE = 2;
    static final int READ_WRITE_onCREATE = 3;
    static final int READ_WRITE_TAKEPICTURE = 4;
    static final int READ_WRITE_GALLERY = 5;
    static final int NEURAL_NETWORK_RUN = 6;

    ImageView mImageView;
    TextView errorMessage;
    File Image = null; //always store the last image taken since app is launched (is no image the content is null)
    static  Boolean saveImages; //saveImages tells if the user wants to keep the pictures it takes
    //if saveImages is false; then privateImmages is true
    static Boolean privateImages; //this tells if the user want to save the picture in the public directory or not
    static File saveFile = null; //file where we save the settings
    static File tempOptions = null; //file where we save the temporary setting of Options Screen
    static Boolean changedOptions = false; //tells us if the option activity was call (we need to change the settings)



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.mimageView);
        errorMessage = (TextView) findViewById(R.id.textView);

        if(!askReadWritePermissions(READ_WRITE_onCREATE)) return;

        //read options or create the file
        saveFile = new File(getExternalFilesDir(null), "options.txt");
        if( !saveFile.exists()) { //if there is no file
            try {
                saveFile.createNewFile();
                writeOptions(true,false);
            } catch (IOException e) {
                return;
            }
            saveImages = true;
            privateImages = false;
        } else { //if there is file
            try {
                readOptions(false); //read file
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //create temp options file
        tempOptions = new File(getExternalFilesDir(null), "temp_options.txt");
        if(!tempOptions.exists()) {
            try {
                tempOptions.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //GO TO CAMERA TO TAKE PICTURE
    public void dispatchTakePictureIntent(View view) {

        if(!askReadWritePermissions(READ_WRITE_TAKEPICTURE)) return; //permissions are not given yet, so end the function here

        //get path to private folder
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
       // File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //create temp-file where the image will be saved
        File image = null;
        try {
            image= File.createTempFile("IMG", ".jpg", storageDir);
        } catch (IOException e) {
            errorMessage.setVisibility(View.VISIBLE);
            Log.e("error", Log.getStackTraceString(e));
            return;
        }

        //delete old image if needed
        if(!saveImages && Image != null) {
            if(Image.exists()) Image.delete();
            Image = null;
        }

        Image = image;

        //get uri of the image
        Uri photoURI = FileProvider.getUriForFile(this, "prova.fileprovider", image);

        //send the intent to the camera
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null ) {
            takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }

    }

    //SELECT PICTURE FROM GALLERY
    public void takePictureFromGalleryIntent(View view){

        if(!askReadWritePermissions(READ_WRITE_GALLERY)) return; //permissions are not given yet, so end the function here

        Intent getGallery = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(getGallery, LOAD_IMAGE);
    }

    //WHAT HAPPENS AFTER INTENT IS EXECUTED
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //AFTER TAKING PICTURE

            //HERE OLD IMAGE IS ALREADY DELETED
            if(!privateImages) { //move file to public directory
                File publicImage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + Image.getName());
                Image.renameTo(publicImage);
                Image = publicImage;
            }

            //load image
            if(Image == null) {
                errorMessage.setVisibility(View.VISIBLE);
                return;
            }
            mImageView.setImageURI(Uri.fromFile(Image));
            mImageView.setVisibility(View.VISIBLE);

            if(!privateImages) { //update gallery to add image
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(Image);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
        }
        else if( requestCode == LOAD_IMAGE && resultCode == RESULT_OK) { //AFTER CHOOSING PICTURE FROM GALLERY

            //delete old image if needed
            if(!saveImages && Image != null) {
                if(Image.exists()) Image.delete();
                Image = null;
            }

            Uri photoURI = data.getData();
            Image = new File (photoURI.toString());
            mImageView.setImageURI(photoURI);
            mImageView.setVisibility(View.VISIBLE);
        }

    }

    //RUN THE NEURAL NETWORK
    public void runNeuralNetwork(View view){

        //ASK PERMISSIONS IF NECESSARY
        if(!askReadWritePermissions(NEURAL_NETWORK_RUN) && !askInternetPermissions(NEURAL_NETWORK_RUN)) return; //permissions are not given yet, so end the function here

        //HANDLE NULL IMAGE FILE
        if(Image == null) {
            errorMessage.setText("error: Image == null");
            errorMessage.setVisibility(View.VISIBLE);
            return;
        }

        Intent ResultIntent = new Intent(MainActivity.this, ResultActivity.class);
        ResultIntent.putExtra("path", Image.getAbsolutePath());
        ResultIntent.putExtra("name", Image.getName());
        startActivity(ResultIntent);



    }

    //send intent to go to options screen
    public void Options(View view) {
        changedOptions = true;
        Intent OptionsIntent = new Intent(MainActivity.this, OptionsActivity.class);
        OptionsIntent.putExtra("saveImages", saveImages);
        OptionsIntent.putExtra("privateImages", privateImages);
        startActivity(OptionsIntent);
    }

    public void SleepMode (View view) {
        Intent sleepIntent = new Intent(MainActivity.this, SleepActivity.class);
        sleepIntent.putExtra("save", saveImages);
        sleepIntent.putExtra("private",privateImages);
        startActivity(sleepIntent);
    }

    @Override
    public void onDestroy() { //when the app is getting killed

        if(!saveImages && Image != null) { //delete images; images are private
            if (Image.exists()) Image.delete();
            Image = null;
        }

        Intent intent = new Intent(this, SleepService.class);
        stopService(intent);
        super.onDestroy();
    }

    @Override
    public void onResume() {

        if(changedOptions) { //if we just got out of the options screen
            changedOptions = false;
            try {
                readOptions(true); //read file
                writeOptions(saveImages,privateImages);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

       // errorMessage.setVisibility(View.VISIBLE);
       // errorMessage.setText(saveImages + " " + privateImages);

        super.onResume();
    }

    private void readOptions(Boolean temp) throws  IOException{

        if(!askReadWritePermissions(0)) return; //permissions are not given yet, so end the function here

        FileReader file = null;

        if (temp) file = new FileReader(tempOptions);
        else file = new FileReader(saveFile);

        BufferedReader read = new BufferedReader(file);

        String line = read.readLine();

        if(line.equals("true false")) {
            saveImages = true;
            privateImages = false;
        }
        else if (line.equals("true true")) {
            saveImages = true;
            privateImages = true;
        }
        else {
            saveImages = false;
            privateImages = true;
        }

        file.close();
        read.close();
    }

    private void writeOptions (Boolean save, Boolean pri) throws  IOException {

        if(!askReadWritePermissions(0)) return; //permissions are not given yet, so end the function here

        saveFile = new File(getExternalFilesDir(null), "options.txt");
        FileOutputStream stream = new FileOutputStream(saveFile);
        stream.write((save.toString()+" "+pri.toString()).getBytes());
        stream.close();

    }

    private boolean askReadWritePermissions(int code) {
        //return true if permissions are already given; false otherwise
        //ask write permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    code);

            return false;

        }
        //ask read permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    code);

            return false;

        }

        return true;
    }

    private boolean askInternetPermissions(int code) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.INTERNET},
                    code);

            return false;

        }if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    code);

            return false;

        }
        return  true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_onCREATE: //permissions asked by onCreate

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do all onCreate actions
                    //read options or create the file
                    saveFile = new File(getExternalFilesDir(null), "options.txt");
                    if( !saveFile.exists()) { //if there is no file
                        try {
                            saveFile.createNewFile();
                            writeOptions(true,false);
                        } catch (IOException e) {
                            return;
                        }
                        saveImages = true;
                        privateImages = false;
                    } else { //if there is file
                        try {
                            readOptions(false); //read file
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    //create temp options file
                    tempOptions = new File(getExternalFilesDir(null), "temp_options.txt");
                    if(!tempOptions.exists()) {
                        try {
                            tempOptions.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }


                } else {

                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText("app cannot run without permission\nClosing in a few seconds");

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, 5000);
                }
                return;

            case READ_WRITE_TAKEPICTURE :
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.button)).performClick();
                }
                return;

            case READ_WRITE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.button2)).performClick();
                }
                return;

            case NEURAL_NETWORK_RUN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.button3)).performClick();
                }
                return;
        }
    }




}


