package com.example.inclass06_api_json;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button goButton;
    private AlertDialog.Builder builder;
    private TextView textView;
    private String selectedKeyword;
    private ProgressBar progressBar;
    private String apiKEY;
    private int index;
    private Bitmap DisplayImage;
    private ImageView selectedImage;
    private ImageView prevImage;
    private ImageView nextImage;
    private ArrayList<Articles> result = new ArrayList<>();
    private TextView title;
    private TextView publishedAt;
    private TextView description;
    private TextView newsCount;
    private String imageUrl;
    private int temp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView_searchID);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        apiKEY = "abe4f6f3d85941a0acd20aff2c9efa7e";
        index = 0;
        selectedImage = findViewById(R.id.imageView_MainID);
        prevImage = findViewById(R.id.imageView_Prev);
        title = findViewById(R.id.textView_Title);
        publishedAt = findViewById(R.id.textView_PublishedAt);
        description = findViewById(R.id.textView_Description);
        nextImage = findViewById(R.id.imageView_Next);
        newsCount = findViewById(R.id.textView_Count);
        prevImage.setEnabled(false);
        nextImage.setEnabled(false);

        goButton = findViewById(R.id.go_btn);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Business", "Entertainment", "General", "Health", "Science", "Sports", "Technology"};
                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Choose Category");
                builder.setCancelable(false);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        Log.d("demo", "onClick" + options[item]);
                        textView.setText(options[item]);
                        selectedKeyword = (String) options[item];
                        progressBar.setVisibility(View.VISIBLE);
                        //goButton.setEnabled(false);
                        index = 0;
                        if(isConnected()) {
                            new GetNews().execute("https://newsapi.org/v2/top-headlines?category=" + selectedKeyword + "&apiKey=" + apiKEY);
                        }
                        else{
                            Toast.makeText(MainActivity.this, "NO Internet Please Check Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                builder.show();
            }

        });
        prevImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "image imdex old : " + index);
                progressBar.setVisibility(View.VISIBLE);
                temp = (Integer)index - 1;
                index = temp;
                Log.d("demo", "image imdex new : " + index);
                nextImage.setEnabled(true);
                if(index == 0) {
                    prevImage.setEnabled(false);
                }else if(index < result.size()-1) {
                    prevImage.setEnabled(true);
                }
                title.setText(result.get(index).getTitle());
                publishedAt.setText(result.get(index).getPublishedAt());
                description.setText(result.get(index).getDescription());
                newsCount.setText((index + 1) + " out of " + result.size());
                prevImage.setEnabled(false);
                imageUrl = result.get(index).getUrl();
                if (imageUrl.trim() != "null" ){
                    progressBar.setVisibility(View.VISIBLE);
                    Log.d("demo", "loop : " + imageUrl);
                    new GetImages(selectedImage).execute(imageUrl);
                }else if(imageUrl == "null") {
                    prevImage.setEnabled(true);
                    nextImage.setEnabled(true);
                    progressBar.setVisibility(View.INVISIBLE);
                    selectedImage.setImageResource(R.drawable.tranparent);
                }
            }
        });

        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.d("demo", "image imdex old : " + index);
                //Log.d("demo", "Array: " + result.size());
                prevImage.setEnabled(true);
                temp = (Integer)index + 1;
                index = temp;
                progressBar.setVisibility(View.VISIBLE);
                title.setText(result.get(index).getTitle());
                publishedAt.setText(result.get(index).getPublishedAt());
                description.setText(result.get(index).getDescription());
                newsCount.setText((index+1) + " out of " + result.size());
                imageUrl = result.get(index).getUrl();
                //Log.d("demo", "length : " + imageUrl.length());
                if (imageUrl.trim() != "null" ){
                    Log.d("demo", "loop url : " + result.get(index).getUrl());
                    Log.d("demo", "loop : " + index);
                    progressBar.setVisibility(View.VISIBLE);
                    new GetImages(selectedImage).execute(imageUrl);
                }else if(imageUrl == "null"){
                    Log.d("demo", "do something : " + index);
                    progressBar.setVisibility(View.INVISIBLE);
                    selectedImage.setImageResource(R.drawable.tranparent);
                    nextImage.setEnabled(true);
                }
                Log.d("demo", "url : " + result.get(index).getUrl());
                if(index == result.size()-1){
                    nextImage.setEnabled(false);
                }
            }
        });
    }
    class GetImages extends AsyncTask<String, Void, Void>{

        ImageView imageView;
        Bitmap bitmap = null;

        public GetImages(ImageView iv) {
            imageView = iv;
        }

        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection connection = null;
            bitmap = null;
            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //Close open connections and reader
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
                progressBar.setVisibility(View.INVISIBLE);
                prevImage.setEnabled(true);
                nextImage.setEnabled(true);
                if(index == 0) {
                    prevImage.setEnabled(false);
                }if(index == result.size()-1) {
                    nextImage.setEnabled(false);
                }
            }
        }

    }
    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                        && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
            return false;
        }
        return true;
    }

    class GetNews extends AsyncTask<String, Void, ArrayList<Articles>> {
        @Override
        protected void onPostExecute(ArrayList<Articles> result) {
            if (result.size() >= 0) {
                Log.d("demo", "result : " + result.toString());
                title.setText(result.get(index).getTitle());
                publishedAt.setText(result.get(index).getPublishedAt());
                description.setText(result.get(index).getDescription());
                newsCount.setText((index+1) + " out of " + result.size());
                imageUrl = result.get(index).getUrl();
                if (imageUrl.trim() != "null" ){
                    new GetImages(selectedImage).execute(imageUrl);
                }else if (imageUrl.trim() == "null" ){
                    progressBar.setVisibility(View.INVISIBLE);
                    selectedImage.setImageResource(R.drawable.tranparent);
                }
            } else {
                Log.d("demo", "Null Result");
            }
        }

        @Override
        protected ArrayList<Articles> doInBackground(String... strings) {
            HttpURLConnection connection = null;
            URL url;
            //String result = null;
            result = new ArrayList<>();
        try {
                url = new URL(strings[0]);
                Log.d("demo", "result : " + url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = IOUtils.toString(connection.getInputStream(), "UTF8");
                    JSONObject root = new JSONObject(json);
                    JSONArray articles = root.getJSONArray("articles");
                    for (int i=0; i< articles.length(); i++) {
                        JSONObject articleJson = articles.getJSONObject(i);
                        Articles article = new Articles();
                        article.title = articleJson.getString("title");
                        article.description = articleJson.getString("description");
                        article.url = articleJson.getString("urlToImage");
                        article.publishedAt = articleJson.getString("publishedAt");
                        result.add(article);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
            e.printStackTrace();
        } finally {
                //Close open connections and reader
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return result;
        }
    }
}
