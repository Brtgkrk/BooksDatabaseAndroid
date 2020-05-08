package com.example.booksdatabasev1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.se.omapi.Session;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ProgressDialog p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getData();
    }

    private void getData() {
        SessionManagement sessionManagement = new SessionManagement(MainActivity.this);
        Map<String,String>userPref = sessionManagement.getSession();

        int userID = Integer.parseInt(userPref.get("id"));
        String userName = userPref.get("username");
        String userPassword = userPref.get("password");

        if(userID == -1){ //user is logged
            moveToLogin();
        }
        else{
            String username = userName;
            String password = userPassword;

            String type = "login";

            TextView helloT = (TextView)findViewById(R.id.helloT);
            helloT.setText("Witaj " + userName);

            BackgroundWorker backgroundWorker = new BackgroundWorker(this);
            backgroundWorker.execute(type, username, password);
        }
    }

    public void logout(View view) {
        SessionManagement sessionManagement = new SessionManagement((MainActivity.this));
        sessionManagement.removeSession();

        moveToLogin();
    }

    private void moveToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class BackgroundWorker extends AsyncTask<String, Void, String> {
        Context context;
        AlertDialog alertDialog;
        String user_name, password;
        BackgroundWorker (Context ctx) {
            context = ctx;
        }
        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            String login_url = "https://jkrok.pl/books_android/getData.php";
            user_name = params[1];
            password = params[2];
            if(type.equals("login")) {
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("login","UTF-8")+"="+URLEncoder.encode(user_name,"UTF-8")+"&"
                            +URLEncoder.encode("password","UTF-8")+"="+URLEncoder.encode(password,"UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    outputStream.close();
                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
                    String result="";
                    String line="";
                    while((line = bufferedReader.readLine())!= null) {
                        result += line;
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return result;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Dane");
            p = new ProgressDialog(MainActivity.this);
            p.setMessage("Pobieranie danych");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                result = new String(result.getBytes("ISO-8859-1"), "UTF-8");
            } catch (java.io.UnsupportedEncodingException e) {
                result = "-1";
            }
            p.hide();

            String message = "";

            try {
                JSONArray books = new JSONArray(result);

                for(int i = 0; i < books.length(); i++){
                    JSONObject b = books.getJSONObject(i);
                    String bookName = b.getString("title");
                    String bookLength = b.getString("pages");
                    String bookAuthor = b.getString("author");
                    String bookGenre = b.getString("genre");
                    String bookCompletion = b.getString("completion");
                    String bookRating = b.getString("rating");
                    String bookDescription = b.getString("description");

                    LinearLayout root = (LinearLayout) findViewById(R.id.bookRoot);
                    TextView textView = new TextView(MainActivity.this);
                    //TODO: Remove all childs of root

                    message = bookName + ", autor: " + bookAuthor + ", gatunek: " + bookGenre + ", ilość stron: " + bookLength + ", rating: " + bookRating;
                    textView.setText(message);


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                    );


                    textView.setLayoutParams(params);
                    root.addView(textView);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                message = "Error in parsing JSON, sorry";
            }

            alertDialog.setMessage(result);
            //alertDialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}
