package oxfordteam5.DeeplyDecodedDisability;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;


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
    static final int PLACE_PICKER = 10;

    //VARIABLES
    ImageView mImageView;
    TextView errorMessage;
    File Image = null; //always store the last image taken since app is launched (is no image the content is null)
    String photoDir = null;
    Utilities util;
    GeoDataClient myClient; //for Google Places API

    //SETTINGS VARIABLES
    static Boolean saveImages; //saveImages tells if the user wants to keep the pictures it takes
    static Boolean gallery; //says if the current Image comes from the gallery or not
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

        util = new Utilities(this);
        myClient = Places.getGeoDataClient(this);

        gallery = false;

        if(!askReadWritePermissions(READ_WRITE_onCREATE)) return;

        //create temp options file
        tempOptions = new File(getExternalFilesDir(null), "temp_options.txt");
        if(!tempOptions.exists()) {
            try {
                tempOptions.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //read options or create the file
        saveFile = new File(getExternalFilesDir(null), "options.txt");
        if( !saveFile.exists()) { //if there is no file
            try {
                saveFile.createNewFile();
                writeOptions(true, true, true);
            } catch (IOException e) {
                return;
            }
            saveImages = true;
            focusCamera = true;
            rotateBitmap = true;
        } else { //if there is file
            try {
                readOptions(false);
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
        if(!saveImages && Image != null && !gallery) {
            if(Image.exists()) Image.delete();
            Image = null;
        }

        Image = image;

        gallery = false;

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
    public void PlacePicker (View view) {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //Intent intent = new Intent(this, MapsActivity.class);
        //startActivity(intent);

       /* if(!askReadWritePermissions(READ_WRITE_INSTANTPICTURE)) return; //permissions are not given yet, so end the function here
        if(!askCameraPermissions(ACCESS_CAMERA)) return;

        //take picture
        util.takePictureAndDisplay(Image,focusCamera,saveImages,gallery,rotateBitmap,mImageView,errorMessage);

        new Thread(new Runnable() { //create new thread which waits untill util is done and then updates image and photo
            @Override
            public void run() {
                while (Utilities.working) {} //wait for the pucture to be taken
                //update files
                Image = util.Image;
                photoDir = util.photoDir;
                gallery = false;
            }
        }).start();*/

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

        Log.e("Main","path: "+photoDir+"; name:"+ Image.getName());
        Intent ResultIntent = new Intent(MainActivity.this, ResultActivity.class);
        ResultIntent.putExtra("path", photoDir);
        ResultIntent.putExtra("name", Image.getName());
        startActivity(ResultIntent);

    }

    //GO TO THE OPTIONS SCREEN
    public void Options(View view) {
        changedOptions = true;
        try {
            readOptions(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent OptionsIntent = new Intent(MainActivity.this, OptionsActivity.class);
        OptionsIntent.putExtra("saveImages", saveImages);
        OptionsIntent.putExtra("rotateBitmap", rotateBitmap);
        OptionsIntent.putExtra("focusCamera", focusCamera);
        startActivity(OptionsIntent);
    }

    //ACTIVATE SLEEP MODE
    public void SleepMode (View view) {
        Intent sleepIntent = new Intent(MainActivity.this, SleepActivity.class);
        sleepIntent.putExtra("save", saveImages);
        sleepIntent.putExtra("focusCamera", focusCamera);
        startActivity(sleepIntent);
    }

    //WHAT HAPPENS AFTER INTENT IS EXECUTED
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) { //AFTER TAKING PICTURE

            //HERE OLD IMAGE IS ALREADY DELETED
            if(saveImages) { //move file to public directory
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

            util.displayImage(mImageView,errorMessage,rotateBitmap,photoDir);

            if(saveImages) { //update gallery to add image
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(Image);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            }
        }
        else if( requestCode == LOAD_IMAGE && resultCode == RESULT_OK) { //AFTER CHOOSING PICTURE FROM GALLERY

            //delete old image if needed
            if(!saveImages && Image != null && !gallery) {
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
            gallery = true;
            util.displayImage(mImageView,errorMessage,rotateBitmap,photoDir);
            Log.e("ads",photoDir);
        }
        else if (requestCode == PLACE_PICKER && resultCode == RESULT_OK) { //we got a place

            mImageView.setImageDrawable( getResources().getDrawable(R.drawable.gears));
            mImageView.setVisibility(View.VISIBLE);

            String placeID = PlacePicker.getPlace( this, data).getId(); //get place ID

            //create log file and write 0 in it
            try {
                File logFile = new File(MainActivity.this.getExternalFilesDir(null),"log.txt");
                if(!logFile.exists()) logFile.createNewFile();
                FileOutputStream stream = new FileOutputStream(logFile);
                String zero = "0";
                stream.write(zero.getBytes());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            Task<PlacePhotoMetadataResponse> photosResponse = myClient.getPlacePhotos(placeID);
            //now set what to do when the task is completed
            photosResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {

                    PlacePhotoMetadataResponse response = task.getResult(); // get response
                    PlacePhotoMetadataBuffer photoBuffer = response.getPhotoMetadata(); //get buffer with list of photos
                    final int bufferSize = photoBuffer.getCount();

                    final Intent multipleUploads = new Intent(MainActivity.this, MultipleResutlsActivity.class);
                    multipleUploads.putExtra("filesNumber", bufferSize);

                    for (int i=0; i<bufferSize; i++) { //for each photo download it and save it in the device

                        PlacePhotoMetadata photoMetadata = photoBuffer.get(i); //get  photo

                        Task<PlacePhotoResponse> pictureResponse = myClient.getPhoto(photoMetadata); //task to download the photo
                        //now set what to do when the photo is downloaded
                        pictureResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {

                                Bitmap bitmap = task.getResult().getBitmap(); //get bitmap

                                //save bitmap to file
                                FileOutputStream savePhoto = null;
                                File storageDir= MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                File image = null;
                                try {
                                    image = File.createTempFile("IMG", ".png", storageDir);
                                    savePhoto = new FileOutputStream(image);
                                    bitmap.compress(Bitmap.CompressFormat.PNG,100,savePhoto);
                                    savePhoto.close();
                                } catch ( Exception e) {
                                    e.printStackTrace();
                                }

                                //look at log and increase counter
                                int count =0;
                                try {
                                    File logFile = new File(MainActivity.this.getExternalFilesDir(null),"log.txt");
                                    FileReader file = new FileReader(logFile);
                                    BufferedReader read = new BufferedReader(file);
                                    String line = read.readLine(); //read line
                                    read.close();
                                    file.close();
                                    count = Integer.parseInt(line); //get counter
                                    FileOutputStream stream = new FileOutputStream(logFile);
                                    stream.write(Integer.toString(count+1).getBytes()); //output new counter
                                    stream.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }

                                multipleUploads.putExtra("path"+count, image.getAbsolutePath());
                                multipleUploads.putExtra("name"+count,image.getName());


                                if (count == bufferSize-1) { //the this was last photo
                                    startActivity(multipleUploads);
                                }

                            } //end of onComplete for pictureResponse (getPhoto)
                        });

                    } //end of loop

                    photoBuffer.release();

                } //end of onComplete for photoResponse (getPlacePhotos)
            });
        }

    }

    //EXECUTE WHEN DESTROYING APP
    @Override
    public void onDestroy() { //when the app is getting killed

        if(!saveImages && Image != null) { //delete images; images are private
            if (Image.exists()) Image.delete();
            Image = null;
        }

        //close the sleep service

        super.onDestroy();
    }

    //EXECUTE WHEN THE APP IS RESUMED
    @Override
    public void onResume() {

        if(changedOptions) { //if we just got out of the options screen
            changedOptions = false;
            try {
                readOptions(true); //read file
                writeOptions(saveImages, rotateBitmap, focusCamera);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        super.onResume();
    }

    //READ OPTIONS AND UPDATE VARIABLES
    private void readOptions(Boolean temp) throws  IOException {
        util.readOptions(false,saveFile,tempOptions); //read file
        saveImages = util.saveImages;
        focusCamera = util.focusCamera;
        rotateBitmap = util.rotateBitmap;
    }

    //WRITE THE OPTIONS PARAMETERS IN THE FILE
    private void writeOptions (Boolean save, Boolean rot, Boolean foc) throws  IOException {

        if(!askReadWritePermissions(0)) return; //permissions are not given yet, so end the function here

        util.writeOptions(save,rot,foc,saveFile);

    }

    //OVERLOADING TO ALLOW MORE FLEXIBILITY
    private void writeOptions (String name , Boolean value) throws  IOException {
        switch (name) {
            case "saveImages":
                writeOptions(value,rotateBitmap,focusCamera);
                return;
            case "rotateBitmap":
                writeOptions(saveImages,value,focusCamera);
                return;
            case "focusCamera":
                writeOptions(saveImages,rotateBitmap,value);
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
                            writeOptions(true,true,true);
                        } catch (IOException e) {
                            return;
                        }
                        saveImages = true;
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

}

