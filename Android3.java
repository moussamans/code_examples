package com.example.capstoneprojecttires;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.HashMap;
import java.util.Map;

public class EmergencyTwoActivity extends Activity {
    private FirebaseFirestore firebasefirestore;
    private RecyclerView TireBrandsRecycler;
    FirestoreRecyclerAdapter brandsAdapter;
    DatabaseReference reference;
    DatabaseReference reference_user;
    DatabaseReference ServiceReference;
    TireOrder tireOrder = new TireOrder();
    Dialog confirmationDialog;
    ImageView exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_two);
        confirmationDialog = new Dialog(this);

        reference = FirebaseDatabase.getInstance().getReference().child("ClientOrders");
        reference_user = FirebaseDatabase.getInstance().getReference().child("Clients");
        final String user_token = FirebaseInstanceId.getInstance().getToken();
        Log.i("User Token", "onCreate: " + user_token);
        ServiceReference = FirebaseDatabase.getInstance().getReference().child("ServiceLocation");
        final Bundle b = getIntent().getExtras();
        firebasefirestore = FirebaseFirestore.getInstance();

        Query getBrands = firebasefirestore.collection(b.getString("StoreName")).whereEqualTo("Size", b.getString("tireSize"));
        TireBrandsRecycler = (RecyclerView) findViewById(R.id.BrandsRecyclerView);
        TireBrandsRecycler.setHasFixedSize(true);
        final FirestoreRecyclerOptions<StoreBrands> brandsOptions = new FirestoreRecyclerOptions.Builder<StoreBrands>().
                setQuery(getBrands, StoreBrands.class).build();
        brandsAdapter = new FirestoreRecyclerAdapter<StoreBrands, BrandViewHolderActivity>(brandsOptions) {
            @Override
            protected void onBindViewHolder(@NonNull final BrandViewHolderActivity brandViewHolderActivity, int i, @NonNull StoreBrands storeBrands) {
                if (storeBrands.getPrice() != 0) {
                    brandViewHolderActivity.tirePrice.setText(storeBrands.getPrice() + "");
                    brandViewHolderActivity.tireBrand.setText(storeBrands.getBrand());
                } else {
                    brandViewHolderActivity.tirePrice.setText("N/A");
                    brandViewHolderActivity.tireBrand.setText(storeBrands.getBrand());
                }
                brandViewHolderActivity.BrandsAvailableView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ServiceReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String token = null;
                                Iterable<DataSnapshot> snapshotIterator = dataSnapshot.getChildren();

                                for (DataSnapshot next : snapshotIterator) {
                                    if (next.child("name").getValue().equals(b.getString("StoreName"))) {
                                        token = next.child("token").getValue().toString();
                                    }
                                }
                                tireOrder.setUser_Name(b.getString("Username"));
                                tireOrder.setPhone(b.getString("Phone"));
                                tireOrder.setStore_Name(b.getString("StoreName"));
                                tireOrder.setBrand(brandViewHolderActivity.tireBrand.getText().toString());
                                tireOrder.setSize(b.getString("tireSize"));
                                tireOrder.setUser_Latitude(b.getDouble("UserLatitude"));
                                tireOrder.setUser_Longitude(b.getDouble("UserLongitude"));
                                tireOrder.setToken(token);
                                tireOrder.setUser_token(user_token);
                                reference.push().setValue(tireOrder);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.w("TAG", "Failed to read value.", error.toException());
                            }
                        });
                        confirmationDialog.setContentView(R.layout.confirmation_popup);
                        exit = (ImageView) confirmationDialog.findViewById(R.id.exitButton);
                        exit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                confirmationDialog.dismiss();
                            }
                        });
                        confirmationDialog.show();
                    }
                });
            }

            @NonNull
            @Override
            public BrandViewHolderActivity onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_cardview, parent, false);
                return new BrandViewHolderActivity(view);
            }

        };

        LinearLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        TireBrandsRecycler.setLayoutManager(layoutManager);
        brandsAdapter.startListening();
        TireBrandsRecycler.setAdapter(brandsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (brandsAdapter != null)
            brandsAdapter.startListening();
    }

}


