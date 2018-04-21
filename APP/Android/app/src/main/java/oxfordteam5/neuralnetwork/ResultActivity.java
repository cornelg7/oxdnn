package oxfordteam5.neuralnetwork;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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

            final MediaType Img = MediaType.parse("image/jpg");

            OkHttpClient.Builder cBuilder = new OkHttpClient.Builder();
            cBuilder.readTimeout(20000, TimeUnit.MILLISECONDS);
            cBuilder.writeTimeout(10, TimeUnit.MINUTES);
            cBuilder.connectTimeout(20000, TimeUnit.MILLISECONDS);

            OkHttpClient client = cBuilder.build();

            RequestBody file_body = RequestBody.create(Img,new File(path));
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("type","image/jpg")
                    .addFormDataPart("picture", name+".jpg", file_body)
                    .build();


            Request request = new Request.Builder()
                    //.url("http://10.0.2.2/~ivomaffei/Oxford/WEB/PHP/upload.php")
                    .url("http://oxdnn.azurewebsites.net/upload")
                    .post(body)
                    //.url("http://10.0.2.2/~ivomaffei/Oxford/WEB/PHP/prova.txt")
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ERROR", "cannot send stuff; response :"+response.toString());
            }

            ResponseBody responseBody = response.body();

            long length =0;
            try {
                length = request.body().contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                return /*response.body().string() +"\n"+*/response.toString()+"\n request method: "+request.method()+" request body length: "+length+" conent type: "+request.body().contentType().toString()+" \n";
                //return  response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "nono";
        }

        protected void onProgressUpdate(Integer... progress) {
           // displayMessage("uploading... " + progress[0]+"% completed");
        }

        protected void onPostExecute(String result) {
            displayMessage(result);
        }

    }



}
