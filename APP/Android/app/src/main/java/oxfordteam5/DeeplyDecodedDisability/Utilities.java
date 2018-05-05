package oxfordteam5.DeeplyDecodedDisability;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ivomaffei on 27/04/18.
 */

public class Utilities {

    Boolean focusCamera, saveImages, rotateBitmap, gallery;
    File Image;
    final String TAG = "UtilitiesNeuralNetwork";
    private Context context;
    private Camera myCamera;
    String photoDir;
    private ImageView imageView;
    private TextView textView;
    static Boolean working; //used to enstablish is work on other threads (take pictures or upload) is done
    TextToSpeech voice;

    public Utilities(Context activity) {
        myCamera = null;
        photoDir = null;
        Image = null;
        focusCamera = null;
        saveImages = null;
        context = activity;
        voice = null;
    }

// UPLOAD FILES ------------------------------------------------------------------------------------------------

    static private class UploadFile extends AsyncTask<String, Integer, String> {

        TextView message;
        TextToSpeech voice;
        ImageView view;
        Context context;
        Boolean delete;
        String path;

        public UploadFile(TextView messageView, ImageView display, TextToSpeech tts, Context act, Boolean del) {
            message = messageView;
            voice = tts;
            context = act;
            view = display;
            delete= del;
        }

        @Override
        protected String doInBackground(String... files) {

            path = files[0];
            String name = files[1];

            if (path == "" || path == null) return "error";
            if (name == "" || name == null) return "error";


            String type = new Utilities(null).getFileExtension(name);
            if (!(type.equals("jpg") || type.equals("png") || type.equals("bmp") || type.equals("gif") || type.equals("jpeg") || type.equals("tiff"))) {
                //name seems not to have an extension
                type = new Utilities(null).getFileExtension(path);
                if (!(type.equals("jpg") || type.equals("png") || type.equals("bmp") || type.equals("gif") || type.equals("jpeg") || type.equals("tiff"))) {
                    Log.e("uploadNeuralNetwork", "cannot find file, abort upload");
                    voice.speak("I don't understand which type of file I'm analysing", TextToSpeech.QUEUE_FLUSH, null, "FileTypeError");
                    return "";
                }
                //the path has an extension, then add it to the name
                name = name + "." + type;
            }

            final MediaType Img = MediaType.parse("image/" + type);

            OkHttpClient.Builder cBuilder = new OkHttpClient.Builder();
            cBuilder.readTimeout(20000, TimeUnit.MILLISECONDS);
            cBuilder.writeTimeout(10, TimeUnit.MINUTES);
            cBuilder.connectTimeout(20000, TimeUnit.MILLISECONDS);

            OkHttpClient client = cBuilder.build();

            RequestBody file_body = RequestBody.create(Img, new File(path));
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type", "image/" + type)
                    .addFormDataPart("picture", name, file_body)
                    .build();


            Request.Builder build = new Request.Builder();
            if(voice != null) build.url("http://oxdnn.uksouth.cloudapp.azure.com/upload-pic-list");
            else build.url("http://oxdnn.uksouth.cloudapp.azure.com/upload-pic-pic");
            build.post(body);
            Request request = build.build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (Exception e) {
                Log.e("ERROR", "cannot send stuff");
                e.printStackTrace();
                return "I can't analyse the image. Probably there is no internet connection";
            }

            try {

                if(voice != null) return response.body().string();
                else {
                    // response is an image, so i save it
                    File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File responseFile = File.createTempFile("IMG", ".jpg", storageDir);
                    FileOutputStream stream = new FileOutputStream(responseFile);
                    stream.write(response.body().bytes());
                    stream.close();

                    response.close();
                    return responseFile.getAbsolutePath();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return "An error occurred. Try again in a few moments";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // displayMessage("uploading... " + progress[0]+"% completed");
        }

        @Override
        protected void onPostExecute(String result) {

            Utilities.working = false;

            //if(message!= null) message.setText(result);
            new Utilities(context).displayImage(view, message, false, result); //display image

            new File(result).delete();

            if( voice != null) {
                //voice.setLanguage(Locale.UK);
                voice.speak(result, TextToSpeech.QUEUE_FLUSH, null, "SleepServiceRequest");
            }

            if (delete) { //remove file uploaded
                new File(path).delete();
            }
        }

    }

    public void upload(TextView err, ImageView display, String path, String name, TextToSpeech voice) {
        upload(err,display,path,name,voice,false);
    }

    public void upload (TextView err, ImageView display, String path, String name, TextToSpeech voice,Boolean delete) {
        working = true;
        Log.e(TAG, "path: " + path + "; name:" + name);

        new UploadFile(err, display, voice, context,delete).execute(path, name); //upload files and show the response in the textview display
    }

    public String getFileExtension(String name) {
        String result = "";

        for (int i = name.length() - 1; i >= 0; i--) {
            char c = name.charAt(i);
            if (c == '.') {
                result = name.substring(i + 1);
            }
        }

        Log.e(TAG, "File type:" + result);

        return result;
    }


    // READ AND WRITE OPTIONS --------------------------------------------------------------------------------------
    //READ OPTIONS FROM FILE; after this call one should look at the variables in util
    public void readOptions(Boolean temp, File saveFile, File tempFile) throws IOException {

        FileReader file = null;

        if (temp) file = new FileReader(tempFile);
        else file = new FileReader(saveFile);

        BufferedReader read = new BufferedReader(file);

        String line = null;

        for (int i = 0; i < 3; ++i) {
            line = read.readLine();
            if (line.equals("true")) {
                if (i == 0) saveImages = true;
                else if (i == 1) rotateBitmap = true;
                else focusCamera = true;
            } else if (line.equals("false")) {
                if (i == 0) saveImages = false;
                else if (i == 1) rotateBitmap = false;
                else focusCamera = false;
            }
        }

        file.close();
        read.close();

    }

    //WRITE THE OPTIONS PARAMETERS IN THE FILE
    public void writeOptions(Boolean save, Boolean rot, Boolean foc, File saveFile) throws IOException {

        //get file and output stream
        saveFile = new File(context.getExternalFilesDir(null), "options.txt");
        FileOutputStream stream = new FileOutputStream(saveFile);
        //write data and close
        stream.write((save.toString() + "\n" + rot.toString() + "\n" + foc.toString()).getBytes());
        stream.close();

    }


// TAKE AND DISPLAY PICTURES -----------------------------------------------------------------------------------

    //SCALE IMAGE AND DISPLAY IT (sometimes image is rotate to fit better; may add option to fix it)
    public void displayImage(ImageView mImageView, TextView errorMessage, Boolean rotate, String photoDir) {

        rotateBitmap = rotate;

        if (photoDir == null) {
            mImageView.setVisibility(View.INVISIBLE);
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText("No Image to display");
            return;
        } else {

            BitmapFactory.Options settings = new BitmapFactory.Options();
            settings.inJustDecodeBounds = true; //so we don't use up much memory
            BitmapFactory.decodeFile(photoDir, settings); //data invariant : photoDir is path of Image
            if (settings.outWidth > 1048 || settings.outHeight > 1048) { //scale the picture if one dimension exceeds 1048px
                int scale = (int) Math.max(settings.outHeight / 1048, settings.outWidth / 1048); //the largest dimension is scale*1048
                settings.inSampleSize = scale; //so the image is 1/scale its original size
            }
            settings.inJustDecodeBounds = false;
            Bitmap ScaledBitmap = BitmapFactory.decodeFile(photoDir, settings); //this time the bitmap is returned
            Bitmap bitmap = null;

            if (rotateBitmap) { //rotate picture to better fit the screen size

                Boolean scrennWider = mImageView.getWidth() > mImageView.getHeight();
                Boolean imageWider = ScaledBitmap.getWidth() > ScaledBitmap.getHeight();
                if (scrennWider != imageWider) { //then rotate
                    //create rotation matrix
                    Matrix matrix = new Matrix();
                    if (scrennWider) matrix.postRotate(-90);
                    else matrix.postRotate(90);

                    //rotate
                    bitmap = Bitmap.createBitmap(ScaledBitmap, 0, 0, ScaledBitmap.getWidth(), ScaledBitmap.getHeight(), matrix, true);
                } else { //no rotation needed
                    bitmap = ScaledBitmap;
                }
            } else { //display image as it is

                //get orientation of the picture
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(photoDir);
                } catch (IOException e) {
                    Log.e("NeuralNetwork", "error with ExifInterface");
                    e.printStackTrace();
                    return;
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                //rotate to real orientation of the picture
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                bitmap = Bitmap.createBitmap(ScaledBitmap, 0, 0, ScaledBitmap.getWidth(), ScaledBitmap.getHeight(), matrix, true);
            }
            errorMessage.setVisibility(View.INVISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(bitmap);
        }
    }

    //Takes picture and saves it
    public void takePicture(File oldImage, Boolean focus, Boolean save, Boolean gal) {
        Image = oldImage;
        focusCamera = focus;
        saveImages = save;
        gallery = gal;
        generalTakePicture(saveCallback);
    }

    //Takes picture, saves it and displays it
    public void takePictureAndDisplay(File oldImage, Boolean focus, Boolean save, Boolean gal, Boolean rot, ImageView imgView, TextView err) {
        Image = oldImage;
        focusCamera = focus;
        saveImages = save;
        rotateBitmap = rot;
        imageView = imgView;
        textView = err;
        gallery = gal;
        generalTakePicture(displayCallback);
    }

    //Takes picture, saves it and uploads it
    public void takePictureAndUpload(File oldImage, Boolean focus, Boolean save, Boolean gal, TextView message, ImageView view, TextToSpeech tts) {
        Image = oldImage;
        focusCamera = focus;
        saveImages = save;
        textView = message;
        imageView = view;
        voice = tts;
        gallery = gal;
        generalTakePicture(uploadCallback);
    }

    //Take picture using the callback given
    private void generalTakePicture(Camera.PictureCallback callback) { //after this is called the activity should update its Image and photoDir

        working = true;

        if (myCamera != null) {
            myCamera.release();
            myCamera = null;
        }

        try {
            myCamera = Camera.open();
        } catch (Exception e) {
            Log.e(TAG, "failed to open Camera");
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
            Log.e(TAG, "error setting camera preview");
            e.printStackTrace();
        }
        myCamera.startPreview();


        if (focusCamera) myCamera.autoFocus(null); //it takes a bit to focus;

        try {
            myCamera.takePicture(null, null, callback);
        } catch (Exception e) {
            Log.e(TAG, "failed to take picture");
            e.printStackTrace();
            return;
        }
    }

    //SAVE the data into a picture
    private void savePicture(byte[] bytes) {
        File storageDir;
        if (!saveImages) storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        else
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        //create temp-file where the image will be saved
        File image = null;
        try {
            image = File.createTempFile("IMG", ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(context.getString(R.string.app_name), Log.getStackTraceString(e));
            return;
        }

        //delete old image if needed
        if (!saveImages && Image != null && !gallery) {
            if (Image.exists()) Image.delete();
            Image = null;
        }
        Image = image;

        //write data collected by camera into the image file
        try {
            FileOutputStream output = new FileOutputStream(Image);
            output.write(bytes);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(context.getString(R.string.app_name), "failed to write data to the picture file");
            return;
        }

        photoDir = Image.getAbsolutePath(); //update directory of photo

        if (saveImages) { //update gallery to add image
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(Image);
            mediaScanIntent.setData(contentUri);
            context.sendBroadcast(mediaScanIntent);
        }
    }

    //CALLBACK WHICH WILL SAVE THE PICTURE TAKEN BY THE CAMERA
    private Camera.PictureCallback saveCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Log.e(TAG, "in the callback");
            savePicture(bytes);
            //release camera
            myCamera.release();
            myCamera = null;
            working = false;
        }
    };

    private Camera.PictureCallback displayCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Log.e(TAG, "in the callback");
            savePicture(bytes);
            //release camera
            myCamera.release();
            myCamera = null;
            displayImage(imageView, textView, rotateBitmap, photoDir);
            working = false;
        }
    };

    private Camera.PictureCallback uploadCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            Log.e(TAG, "in the callback");
            savePicture(bytes);
            //release camera
            myCamera.release();
            myCamera = null;
            upload(textView, imageView, photoDir, Image.getName(), voice);
            working = false;
        }
    };


}

