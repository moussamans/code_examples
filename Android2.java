package com.example.capstoneprojecttires;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EmergencyOneActivity extends AppCompatActivity {
    RecyclerView ServiceLocationRecycler;
    DatabaseReference reference;
    FirebaseFirestore firebaseFirestore;
    final int NUM_SIZES = 95;
    FirebaseRecyclerOptions<UserServiceLocation> locationOptions;
    FirebaseRecyclerAdapter<UserServiceLocation, ServiceViewHolderActivity> locationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_one);
        final Bundle bb = getIntent().getExtras();
        Log.i("Username", "onCreate: " + bb.getString("Username"));
        final Double UserLatitude = bb.getDouble("UserLatitude");
        final Double UserLongitude = bb.getDouble("UserLongitude");
        final AutoCompleteTextView tire_size = (AutoCompleteTextView) findViewById(R.id.tireSizeText);
        reference = FirebaseDatabase.getInstance().getReference().child("ServiceLocation");
        firebaseFirestore = FirebaseFirestore.getInstance();
        Query getSizes = firebaseFirestore.collection("Sizes");
        getSizes.addSnapshotListener(EmergencyOneActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                String[] days = new String[NUM_SIZES];
                for (int i=0; i<NUM_SIZES; i++) {
                    days[i] = value.getDocuments().get(i).get("Size").toString();
                }
                tire_size.setAdapter(new ArrayAdapter<>(EmergencyOneActivity.this,android.R.layout.simple_list_item_1, days));
            }
        });
        ServiceLocationRecycler = (RecyclerView) findViewById(R.id.NearestServiceView);
        ServiceLocationRecycler.setHasFixedSize(true);
        final View StoresAvailableView = findViewById(R.id.StoresAvailableView);
        locationOptions = new FirebaseRecyclerOptions.Builder<UserServiceLocation>().setQuery(reference, UserServiceLocation.class).build();
        locationAdapter = new FirebaseRecyclerAdapter<UserServiceLocation, ServiceViewHolderActivity>(locationOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final ServiceViewHolderActivity serviceViewHolderActivity, final int i, @NonNull final UserServiceLocation userServiceLocation) {
                Double distance = 6374 * Math.acos(Math.cos(Math.toRadians(33.8428738)) * Math.cos(Math.toRadians(userServiceLocation.getLatitude()))
                        * Math.cos(Math.toRadians(userServiceLocation.getLongitude()) - Math.toRadians(35.5127525))
                        + Math.sin(Math.toRadians(33.8428738)) * Math.sin(Math.toRadians(userServiceLocation.getLatitude())));
                /*33.8428738 35.5127525 b.getDouble("UserLatitude")
                33.4349002 35.4225883
                Log.d("hello", "value: " + userServiceLocation.getName());
                Log.d("hello", "value: " + distance);
                Log.d("hello", "value: " + b.getDouble("UserLongitude"));*/
                if (distance < 10.0) {
                    serviceViewHolderActivity.txtOne.setText(userServiceLocation.getName());
                    serviceViewHolderActivity.StoresAvailableView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent EmergencyTwoIntent = new Intent(EmergencyOneActivity.this, EmergencyTwoActivity.class);
                            Bundle b = new Bundle();
                            b.putString("tireSize", tire_size.getText().toString().toUpperCase());
                            b.putString("StoreName", serviceViewHolderActivity.txtOne.getText().toString());
                            String username = bb.getString("Username");
                            String phone = bb.getString("Phone");
                            b.putDouble("UserLatitude", UserLatitude);
                            b.putDouble("UserLongitude", UserLongitude);
                            b.putString("Username", username);
                            b.putString("Phone", phone);
                            if (tire_size.getText().toString().equals("")) {
                                Toast toast = Toast.makeText(EmergencyOneActivity.this, "Please enter your wheel size before selecting a store", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            } else {
                                EmergencyTwoIntent.putExtras(b);
                                startActivity(EmergencyTwoIntent);
                            }
                        }
                    });
                    serviceViewHolderActivity.view_store_location.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Uri gmmIntentUri = Uri.parse("geo:" + userServiceLocation.getLatitude() + "," + userServiceLocation.getLongitude() + "?q=" + userServiceLocation.getLatitude() + "," + userServiceLocation.getLongitude());
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                            mapIntent.setPackage("com.google.android.apps.maps");
                            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(mapIntent);
                            }
                        }
                    });

                } else {
                    serviceViewHolderActivity.Store_info.setVisibility(View.INVISIBLE);
                }
            }

            @NonNull
            @Override
            public ServiceViewHolderActivity onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service_cardview, parent, false);
                return new ServiceViewHolderActivity(view);
            }
        };
        LinearLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        ServiceLocationRecycler.setLayoutManager(layoutManager);
        locationAdapter.startListening();
        ServiceLocationRecycler.setAdapter(locationAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationAdapter != null)
            locationAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationAdapter != null)
            locationAdapter.startListening();
    }

}
