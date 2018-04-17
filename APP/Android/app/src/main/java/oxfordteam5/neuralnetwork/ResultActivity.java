package oxfordteam5.neuralnetwork;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ResultActivity extends AppCompatActivity {

    TextView message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        message = findViewById(R.id.textView2);
        Intent start = getIntent();
        String path = start.getStringExtra("path");
        String name = start.getStringExtra("name");

        message.setText(path);
        new UploadFile().execute(path,name);
    }

    void displayMessage(String text) {
        message.setText(text);
    }


    private class UploadFile extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... files) {

            String path = files[0];
            String name = files[1];

            if(path == "" || path == null) return "error";
            if(name == "" || name == null) return "error";

            int serverResponseCode;
            String serverResponseMessage;

            // SEND IMAGE TO SERVER
            try {

                //ESTABLISH THE CONNECTION
                URL url = new URL ("https://oxdnn.azurewebsites.net/upload");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setReadTimeout(15000 /* milliseconds */);
                connection.setConnectTimeout(15000 /* milliseconds */);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;");
                connection.setRequestProperty("uploaded_file",path);

                //GET RESPONSE FROM SERVER
                serverResponseCode = connection.getResponseCode();
                serverResponseMessage = connection.getResponseMessage();
                if(serverResponseCode == 200) { //everything is OK
                    displayMessage("connectiod enstablished");
                }
                else {
                    Log.i("UploadError", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);
                    return "upload failed";
                }

                //WRITE THE FILE TO THE OUTPUTSTREAM OF THE CONNECTION
                DataOutputStream DataOutput = new DataOutputStream(connection.getOutputStream()); //get outputstream where to write the file
                FileInputStream readData = new FileInputStream(path); //get input stream to read the file

                //write the header of the data to send (send it as a html form)
                DataOutput.writeBytes("--" + "*****" + "\r\n");
                DataOutput.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ name + "\"" + "\r\n");
                DataOutput.writeBytes("\r\n");

                //create and array of bytes as buffer (max size = 1MB)
                int bufferSize = bufferSize = Math.min(readData.available(),1024*1024*1);

                byte[] buffer = new byte[bufferSize]; //create buffer

                int bytesToRead = readData.read(buffer,0,bufferSize); //bytes that needs reading

                while (bytesToRead > 0){ //until there are bytes to read
                    DataOutput.write(buffer,0,bufferSize); //write bytes to connection
                    bufferSize = Math.min(readData.available(),1024*1024*1); //update the buffer size
                    bytesToRead = readData.read(buffer,0,bufferSize); //read other bytes
                }

                //write end of the form
                DataOutput.writeBytes("\r\n");
                DataOutput.writeBytes("--" + "*****" + "--" + "\r\n");

                //GET RESPONSE FROM SERVER
                serverResponseCode = connection.getResponseCode();
                serverResponseMessage = connection.getResponseMessage();

                //CLOSE EVERYTHING
                readData.close();
                DataOutput.flush();
                DataOutput.close();

            } catch (Exception e) {
                e.printStackTrace();
                return "exception occurred";
            }

            if(serverResponseCode == 200) { //everything is OK
                return "upload complete successfully";
            }
            else {
                Log.i("UploadError", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);
                return "upload failed";
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            displayMessage("uploading... " + progress[0]+"% completed");
        }

        protected void onPostExecute(String result) {
            displayMessage(result);
        }

    }



}
