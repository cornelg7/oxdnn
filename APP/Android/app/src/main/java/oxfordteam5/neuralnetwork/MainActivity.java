package oxfordteam5.neuralnetwork;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    //CONSTANTS
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int LOAD_IMAGE = 2;
    static final int READ_WRITE_onCREATE = 3;
    static final int READ_WRITE_TAKEPICTURE = 4;
    static final int READ_WRITE_GALLERY = 5;
    static final int NEURAL_NETWORK_RUN = 6;
    static final int READ_WRITE_INSTANTPICTURE = 7;
    static final int ACCESS_CAMERA = 8;
    static final int CAMERA_TAKEPICTURE = 9;

    //VARIABLES
    ImageView mImageView;
    TextView errorMessage;
    Camera myCamera = null;
    File Image = null; //always store the last image taken since app is launched (is no image the content is null)
    String photoDir = null;

    //SETTINGS VARIABLES
    static  Boolean saveImages; //saveImages tells if the user wants to keep the pictures it takes
    //if saveImages is false; then privateImmages is true
    static Boolean privateImages; //this tells if the user want to save the picture in the public directory or not
    static Boolean rotateBitmap; //true to rotate image when displaying them in the mImageView
    static Boolean focusCamera; //true to make the camera focus when taking instant pictures
    static File saveFile = null; //file where we save the settings
    static File tempOptions = null; //file where we save the temporary setting of Options Screen
    static Boolean changedOptions = false; //tells us if the option activity was call (we need to change the settings)

    //execute when app is launched
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.mimageView);
        errorMessage = (TextView) findViewById(R.id.textView);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(!askReadWritePermissions(READ_WRITE_onCREATE)) return;

        //read options or create the file
        saveFile = new File(getExternalFilesDir(null), "options.txt");
        if( !saveFile.exists()) { //if there is no file
            try {
                saveFile.createNewFile();
                writeOptions(true,false, true, true);
            } catch (IOException e) {
                return;
            }
            saveImages = true;
            privateImages = false;
            focusCamera = true;
            rotateBitmap = true;
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
        if(!askCameraPermissions(CAMERA_TAKEPICTURE)) return;

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

    //TAKE INSTANT PHOTO
    public void takePicture (View view) {
        if(!askReadWritePermissions(READ_WRITE_INSTANTPICTURE)) return; //permissions are not given yet, so end the function here
        if(!askCameraPermissions(ACCESS_CAMERA)) return;

        //delete old image if needed
        if(!saveImages && Image != null) {
            if(Image.exists()) Image.delete();
            Image = null;
        }

        if(myCamera != null) {
            myCamera.release();
            myCamera = null;
        }

        try {
            myCamera = Camera.open();
        } catch (Exception e) {
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText("cannot open camera");
            Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
            return;
        }

        Camera.Parameters cameraSettings = myCamera.getParameters();
        cameraSettings.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        cameraSettings.setRotation(90);//set the camera to have the right orientation
        cameraSettings.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        myCamera.setParameters(cameraSettings);


        //create camera preview so we can take picture without problems
        SurfaceTexture surfaceTexture = new SurfaceTexture(10);
        try {
            myCamera.setPreviewTexture(surfaceTexture);
        } catch (IOException e) {
            Log.e("NeuralNetwork", "error setting camera preview");
            e.printStackTrace();
        }
        myCamera.startPreview();


        if(focusCamera) myCamera.autoFocus(null); //it takes a bit to focus;

        try {
            myCamera.takePicture(null, null,myCallback );
        } catch (Exception e) {
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText("cannot take picture");
            Log.e(getString(R.string.app_name), "failed to take picture");
            e.printStackTrace();
            return;
        }

    }

    //SELECT PICTURE FROM GALLERY
    public void takePictureFromGalleryIntent(View view){

        if(!askReadWritePermissions(READ_WRITE_GALLERY)) return; //permissions are not given yet, so end the function here

        Intent getGallery = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(getGallery, LOAD_IMAGE);
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
        ResultIntent.putExtra("path", photoDir);
        ResultIntent.putExtra("name", Image.getName());
        startActivity(ResultIntent);



    }

    //GO TO THE OPTIONS SCREEN
    public void Options(View view) {
        changedOptions = true;
        Intent OptionsIntent = new Intent(MainActivity.this, OptionsActivity.class);
        OptionsIntent.putExtra("saveImages", saveImages);
        OptionsIntent.putExtra("privateImages", privateImages);
        OptionsIntent.putExtra("rotateBitmap", rotateBitmap);
        OptionsIntent.putExtra("focusCamera", focusCamera);
        startActivity(OptionsIntent);
    }

    //ACTIVATE SLEEP MODE
    public void SleepMode (View view) {
        Intent sleepIntent = new Intent(MainActivity.this, SleepActivity.class);
        sleepIntent.putExtra("save", saveImages);
        sleepIntent.putExtra("private",privateImages);
        startActivity(sleepIntent);
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

            photoDir = Image.getAbsolutePath();

            //load image
            if(Image == null) {
                errorMessage.setVisibility(View.VISIBLE);
                return;
            }

            displayImage();

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

            //the code below gets the actual path of the image and stores it into photoDir
            Cursor cursor = null;
            try {
                String[] proj = { MediaStore.Images.Media.DATA };
                cursor = this.getContentResolver().query(photoURI,  proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                photoDir = cursor.getString(column_index);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            //now get a file and set the image view
            Image = new File (photoURI.toString());
            displayImage();
        }

    }

    //SCALE IMAGE AND DISPLAY IT (sometimes image is rotate to fit better; may add option to fix it)
    protected void displayImage() {
        if (Image == null){
            mImageView.setVisibility(View.INVISIBLE);
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText("null image");
            return;
        }
        else {

            BitmapFactory.Options settings = new BitmapFactory.Options() ;
            settings.inJustDecodeBounds = true; //so we don't use up much memory
            BitmapFactory.decodeFile(photoDir, settings); //data invariant : photoDir is path of Image
            if( settings.outWidth > 1048 || settings.outHeight > 1048) { //scale the picture if one dimension exceeds 1048px
                int scale = (int) Math.max(settings.outHeight/1048, settings.outWidth/1048); //the largest dimension is scale*1048
                settings.inSampleSize = scale; //so the image is 1/scale its original size
            }
            settings.inJustDecodeBounds = false;
            Bitmap ScaledBitmap = BitmapFactory.decodeFile(photoDir, settings); //this time the bitmap is returned
            Bitmap bitmap = null;
            if( rotateBitmap ){
                Boolean scrennWider = mImageView.getWidth() > mImageView.getHeight();
                Boolean imageWider = ScaledBitmap.getWidth() > ScaledBitmap.getHeight();
                if( scrennWider != imageWider) { //then rotate
                    //create rotation matrix
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);

                    //rotate
                    bitmap = ScaledBitmap.createBitmap(ScaledBitmap, 0, 0, ScaledBitmap.getWidth(),ScaledBitmap.getHeight(), matrix, true);
                }
            }
            else {
                bitmap = ScaledBitmap;
            }
            errorMessage.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(bitmap);
        }
    }

    //EXECUTE WHEN DESTROYING APP
    @Override
    public void onDestroy() { //when the app is getting killed

        if(!saveImages && Image != null) { //delete images; images are private
            if (Image.exists()) Image.delete();
            Image = null;
        }

        super.onDestroy();
    }

    //EXECUTE WHEN THE APP IS RESUMED
    @Override
    public void onResume() {

        if(changedOptions) { //if we just got out of the options screen
            changedOptions = false;
            try {
                readOptions(true); //read file
                writeOptions(saveImages,privateImages, rotateBitmap, focusCamera);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        super.onResume();
    }

    //READ OPTIONS FROM FILE
    private void readOptions(Boolean temp) throws  IOException{

        if(!askReadWritePermissions(0)) return; //permissions are not given yet, so end the function here

        FileReader file = null;

        if (temp) file = new FileReader(tempOptions);
        else file = new FileReader(saveFile);

        BufferedReader read = new BufferedReader(file);

        String line = read.readLine();

        if(line == null || line.equals("") ) return; //tempfile is empty

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

        for (int i=0; i<2; ++i) {
            line = read.readLine();
            if(line.equals("true")) {
                if(i==1) rotateBitmap = true;
                else focusCamera = true;
            }
            else if (line.equals("false")) {
                if(i==1) rotateBitmap = false;
                else focusCamera = false;
            }
        }

        file.close();
        read.close();
    }

    //WRITE THE OPTIONS PARAMETERS IN THE FILE
    private void writeOptions (Boolean save, Boolean pri, Boolean rot, Boolean foc) throws  IOException {

        if(!askReadWritePermissions(0)) return; //permissions are not given yet, so end the function here

        //get file and output stream
        saveFile = new File(getExternalFilesDir(null), "options.txt");
        FileOutputStream stream = new FileOutputStream(saveFile);
        //write data and close
        stream.write((save.toString()+" "+pri.toString() + "\n"+ rot.toString() + "\n"+foc.toString()).getBytes());
        stream.close();

    }

    //OVERLOADING TO ALLOW MORE FLEXIBILITY
    private void writeOptions (String name , Boolean value) throws  IOException {
        switch (name) {
            case "saveImages":
                writeOptions(value,privateImages,rotateBitmap,focusCamera);
                return;
            case "privateImages":
                writeOptions(saveImages,value,rotateBitmap,focusCamera);
                return;
            case "rotateBitmap":
                writeOptions(saveImages,privateImages,value,focusCamera);
                return;
            case "focusCamera":
                writeOptions(saveImages,privateImages,rotateBitmap,value);
                return;
            default:
                return;
        }
    }

    //ASK FOR READ AND WRITE PERMISSIONS
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

    //ASK FOR INTERNET PERMISSIONS
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

    //ASK FOR CAMERA PERMISSIONS
    private boolean askCameraPermissions(int code) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    code);

            return false;

        }

        return true;
    }

    //EXECUTE AFETER PERMISSION IS GRANTED OR NEGATED
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
                            writeOptions(true,false,true,true);
                        } catch (IOException e) {
                            return;
                        }
                        saveImages = true;
                        privateImages = false;
                        rotateBitmap = true;
                        focusCamera = true;
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

                    mImageView.setVisibility(View.INVISIBLE);
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
                    ((Button) findViewById(R.id.camera)).performClick();
                }
                return;

            case CAMERA_TAKEPICTURE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.camera)).performClick();
                }
                return;

            case READ_WRITE_GALLERY:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.gallery)).performClick();
                }
                return;

            case NEURAL_NETWORK_RUN:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.runNetwork)).performClick();
                }
                return;

            case READ_WRITE_INSTANTPICTURE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.instantPicture)).performClick();
                }
                return;
            case ACCESS_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((Button) findViewById(R.id.instantPicture)).performClick();
                }
                else  {

                    mImageView.setVisibility(View.INVISIBLE);
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText("Cannot take instant picture without permissions");
                }
                return;
        }
    }


    //CALLBACK WHICH WILL SAVE THE PICTURE TAKEN BY THE CAMERA
    private Camera.PictureCallback myCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

            File storageDir;
            if (privateImages) storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            else storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            //create temp-file where the image will be saved
            File image = null;
            try {
                image= File.createTempFile("IMG", ".jpg", storageDir);
            } catch (IOException e) {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText("cannot create file to save picture in");
                Log.e(getString(R.string.app_name), Log.getStackTraceString(e));
                return;
            }

            Image = image; //the Image file should already be deleted before taking the picture

            //write data collected by camera into the image file
            try {
                FileOutputStream output = new FileOutputStream(Image);
                output.write(bytes);
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(getString(R.string.app_name), "failed to write data to the picture file");
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText("cannot write image to file");
                return;
            }

            //release camera
            myCamera.release();
            myCamera = null;

            photoDir = Image.getAbsolutePath(); //update directory of photo

            //put picture into image view
            displayImage();

            if (!privateImages) { //update gallery to add image
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(Image);
                mediaScanIntent.setData(contentUri);
                MainActivity.this.sendBroadcast(mediaScanIntent);
            }

        }
    };
}

