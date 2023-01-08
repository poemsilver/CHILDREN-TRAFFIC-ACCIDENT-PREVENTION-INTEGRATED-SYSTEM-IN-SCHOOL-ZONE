package org.techtown.demo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DriverActivity extends AppCompatActivity implements AutoPermissionsListener {

    private static String TAG = "phptest";

    MediaPlayer mediaPlayer;

    private TextView mTextViewResult;
    static ArrayList mArrayList;
    ListView mlistView;
    String mJsonString;

    SupportMapFragment mapFragment;
    GoogleMap map;

    MarkerOptions myLocationMarker;
    MarkerOptions kidLocationMarker;

    Driverlocation driverlocation = new Driverlocation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        mTextViewResult = (TextView)findViewById(R.id.textView_main_result);
        mlistView = (ListView) findViewById(R.id.listView_main_list);
        mArrayList = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        map = googleMap;
                                    }
                                });
        try {
            MapsInitializer.initialize(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        startLocationService();

        AutoPermissions.Companion.loadAllPermissions(this, 102);

        /*Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                GetData task = new GetData();
                task.execute("http://kominget.paasta.koren.kr/", "");
            }
        });  @@@@@@@@@@@@@@@@@버튼식이에요@@@@@@@@@@@ xml에 버튼 불러오기 하나추가하면댐~*/

        mHandler.sendEmptyMessage(0);

    }
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg){
            GetData task = new GetData();
            task.execute("http://kominget.paasta.koren.kr/", "");

            mHandler.sendEmptyMessageDelayed(0, 1000);
        }
    };

    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            GPSListener gpsListener = new GPSListener();
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000, 0, gpsListener);

            if(location !=null) {
                manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, gpsListener);
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();


            driverlocation.setDriverlatitude(latitude);
            driverlocation.setDriverlongitude(longitude);

            showCurrentLocation(latitude, longitude);
        }

        public void onProviderDisabled(String provider) { }

        public void onProviderEnabled(String provider) { }

        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }

    private void showCurrentLocation(Double latitude, Double longitude) {
        LatLng curPoint = new LatLng(latitude, longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 18));

        showMyLocationMarker(curPoint);
    }

    private void showMyLocationMarker(LatLng curPoint) {

        if (myLocationMarker == null) {
            myLocationMarker = new MarkerOptions();
            myLocationMarker.position(curPoint);
            myLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.drivericon));
            map.addMarker(myLocationMarker);
        } else {
            myLocationMarker.position(curPoint);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }

    @Override
    public void onDenied(int requestCode, @Nullable String[] permissions){
        Toast.makeText(this, "permissions denied : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGranted(int requestCode, @Nullable String[] permissions){
        Toast.makeText(this, "permissions granted : " + permissions.length, Toast.LENGTH_LONG).show();
    }

    private class GetData extends AsyncTask<String, Void, String> {


        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null){
                mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

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
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();

            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    private void showResult(){

        String TAG_JSON="komin-e";
        String TAG_ID = "id";
        String TAG_NAME = "name";
        String TAG_AGE ="age";
        String TAG_GENDER = "gender";
        String TAG_LATITUDE = "latitude";
        String TAG_LONGITUDE = "longitude";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                String id = item.getString(TAG_ID);
                String name = item.getString(TAG_NAME);
                String age = item.getString(TAG_AGE);
                String gender = item.getString(TAG_GENDER);
                String latitude = item.getString(TAG_LATITUDE);
                String longitude = item.getString(TAG_LONGITUDE);


                Kid kid = new Kid();

                kid.setId(item.getString(TAG_ID));
                kid.setName(item.getString(TAG_NAME));
                kid.setAge(item.getString(TAG_AGE));
                kid.setGender(item.getString(TAG_GENDER));
                kid.setLatitude(item.getString(TAG_LATITUDE));
                kid.setLongitude(item.getString(TAG_LONGITUDE));

                mArrayList.add(kid);

                String skidLatitude = kid.getLatitude();
                String skidLongitude = kid.getLongitude();

                double kidLatitude = Double.valueOf(skidLatitude).doubleValue();
                double kidLongitude = Double.valueOf(skidLongitude).doubleValue();
                LatLng child1 = new LatLng(kidLatitude, kidLongitude);

                kidLocationMarker = new MarkerOptions();
                kidLocationMarker.position(child1);
                kidLocationMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.kid));
                map.addMarker(kidLocationMarker);

                Log.d("내용",  (kid.getId()));
                Log.d("내용",  (kid.getName()));
                Log.d("내용",  (kid.getAge()));
                Log.d("내용",  (kid.getGender()));
                Log.d("내용",  (kid.getLatitude()));
                Log.d("내용",  (kid.getLongitude()));


                double driverLatitude = driverlocation.getDriverlatitude();
                double driverLongitude = driverlocation.getDriverlongitude();

                Log.d("운전자위도 : " , String.valueOf(driverLatitude));
                Log.d("운전자경도 : " , String.valueOf(driverLongitude));
                Log.d("어린이위도 : " , String.valueOf(kidLatitude));
                Log.d("어린이경도 : " , String.valueOf(kidLongitude));

                Location locationKid = new Location("point A");
                locationKid.setLatitude(kidLatitude);
                locationKid.setLongitude(kidLongitude);

                Location locationDriver = new Location("point B");
                locationDriver.setLatitude(driverLatitude);
                locationDriver.setLongitude(driverLongitude);

                float distance = locationDriver.distanceTo(locationKid);

                Log.d("어린이 좌표 : " , String.valueOf(locationKid));
                Log.d("운전자 좌표 : " , String.valueOf(locationDriver));

                Log.d(": 거리 : " , String.valueOf(distance));

                if( distance < 5 ){

                    mediaPlayer = MediaPlayer.create(DriverActivity.this,R.raw.war2);
                    mediaPlayer.start();

                }
            }

            ListAdapter adapter = new SimpleAdapter(
                    DriverActivity.this, mArrayList, R.layout.item_list,
                    new String[]{TAG_ID,TAG_NAME, TAG_AGE, TAG_GENDER, TAG_LATITUDE, TAG_LONGITUDE},
                    new int[]{R.id.textView_list_id, R.id.textView_list_name, R.id.textView_list_age,
                            R.id.textView_list_gender,R.id.textView_list_latitude,R.id.textView_list_longitude}
            );

            mlistView.setAdapter(adapter);

        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }



    }
}