package org.techtown.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class KidActivity extends AppCompatActivity {

    EditText editText;
    EditText editText2;

    String gender;
    String kidname;
    String kidage;

    double slatitude;
    double slongitude;


    private static String TAG = "phptest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kid);

        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                kidname = editText.getText().toString();
                kidage = editText2.getText().toString();

                CheckBox checkBox = findViewById(R.id.checkBox);

                if (checkBox.isChecked()) {
                    gender = ("남자");
                } else {
                    gender = ("여자");
                }
                show();

            }
        });

        AutoPermissions.Companion.loadAllPermissions(this, 101);
    }

    void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("확인");
        builder.setMessage("이름 : " + kidname + "\n나이 : " + kidage + "\n성별 : " + gender + "가 맞습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startLocationService();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.show();
    }

    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            try {
                Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    slatitude = location.getLatitude();
                    slongitude = location.getLongitude();
                }

            GPSListener gpsListener = new GPSListener();
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000, 0, gpsListener);
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, gpsListener);
            } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {

            slatitude = location.getLatitude();
            slongitude = location.getLongitude();

            InsertData task = new InsertData();
            String latitude = String.valueOf(slatitude);
            String longitude = String.valueOf(slongitude);
            task.execute("http://komine.paasta.koren.kr/", kidname, kidage, gender, latitude, longitude);

        }
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    class InsertData extends AsyncTask<String, Void, String>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(KidActivity.this,"Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "POST response - " + result);
        }

        @Override
        protected String doInBackground(String... params){

            String name = (String)params[1];
            String age = (String)params[2];
            String gender = (String)params[3];
            String latitude = (String)params[4];
            String longitude = (String)params[5];

            String serverURL = (String)params[0];
            String postParameters = "name=" + name
                    + "&age=" + age + "&gender=" + gender + "&latitude=" + latitude + "&longitude=" + longitude;

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }
        }
    }
}