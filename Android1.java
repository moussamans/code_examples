package com.example.capstoneprojecttires;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.AndroidException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.capstoneprojecttires.Signup_Login.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomePage extends AppCompatActivity {

    DatabaseReference reference;
    Double UserLatitude;
    Double UserLongitude;
    UserServiceLocation userlocation;
    ImageView signoutButton;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        final View EmergencyView = findViewById(R.id.EmergencyButton);
        final View StoreView = findViewById(R.id.StoreButton);

        signoutButton = (ImageView) findViewById(R.id.signoutButton);
        final Bundle bb = getIntent().getExtras();
        Log.i("UserName", "onC: "+bb.getString("Username"));
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            TurnOnGps();
        }
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Map<String, Object> userloc = new HashMap<>();

        reference = FirebaseDatabase.getInstance().getReference().child("UserLocation");
        userlocation = new UserServiceLocation();
        client = LocationServices.getFusedLocationProviderClient(this);

        EmergencyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(HomePage.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(HomePage.this, new String[]{ACCESS_FINE_LOCATION}, 1);
                }
                client.getLastLocation().addOnSuccessListener(HomePage.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        try {
                            UserLatitude = location.getLatitude();
                            UserLongitude = location.getLongitude();

                        userlocation.setName(bb.getString("UserName"));
                        userlocation.setLatitude(UserLatitude);
                        userlocation.setLongitude(UserLongitude);
                        userloc.put("name", bb.getString("UserName"));
                        userloc.put("latitude", UserLatitude);
                        userloc.put("longitude", UserLongitude);

                        Intent EmergencyIntent = new Intent(HomePage.this, EmergencyOneActivity.class);
                        Bundle b = new Bundle();
                        String username = bb.getString("Username");
                        String phone = bb.getString("Phone");
                        b.putDouble("UserLatitude", UserLatitude);
                        b.putDouble("UserLongitude", UserLongitude);
                        b.putString("Username",username);
                        b.putString("Phone",phone);
                        EmergencyIntent.putExtras(b);
                        startActivity(EmergencyIntent);
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            startActivity(getIntent());
                        }
                    }
                });
            }
        });

        signoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent SignoutIntent= new Intent(HomePage.this, LoginActivity.class);
                startActivity(SignoutIntent);
            }
        });
        StoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent StoreIntent = new Intent(HomePage.this, offers_ourStore.class);
                startActivity(StoreIntent);
            }
        });

    }

    private void TurnOnGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?").setCancelable(false)
                .setPositiveButton("Turn on GPS", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

   /* @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }*/

    @Override
    public void onBackPressed() {

    }
}
