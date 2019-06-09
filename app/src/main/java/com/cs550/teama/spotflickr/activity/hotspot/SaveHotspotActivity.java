package com.cs550.teama.spotflickr.activity.hotspot;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.PhotoListActivity;
import com.cs550.teama.spotflickr.model.Hotspot;
import com.cs550.teama.spotflickr.model.HotspotList;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SaveHotspotActivity extends AppCompatActivity implements View.OnClickListener {

    private String content;
    private String lat;
    private String lon;

    private EditText editTextDesc;
    private Spinner spinner;

    private int spinner_position;
    private List<String> hotspot_list_name;
    private List<String> hotspot_list_id;
    private ArrayAdapter<String> adapter;
    private List<DocumentSnapshot> list;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference hotspotListCollection = db.collection("hotspot lists");
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_hotspot);

        Bundle extras = getIntent().getExtras();
        content = extras.getString("content");
        lat = extras.getString("latitude");
        lon = extras.getString("longitude");

        TextView textView_content = (TextView) findViewById(R.id.textview_content);
        TextView textView_latitude = (TextView) findViewById(R.id.textview_latitude);
        TextView textView_longitude = (TextView) findViewById(R.id.textview_longitude);

        textView_content.setText(content);
        textView_latitude.setText(lat);
        textView_longitude.setText(lon);

        spinner = (Spinner) findViewById(R.id.spinner_hotspot_list);
        hotspot_list_name = new ArrayList<String>();
        hotspot_list_id = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, hotspot_list_name);
        spinner.setAdapter(adapter);
        hotspotListCollection.whereEqualTo("user_id", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        list = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot d : list) {
                            HotspotList hList = d.toObject(HotspotList.class);
                            hList.setListId(d.getId());
                            hotspot_list_name.add(hList.getName());
                            hotspot_list_id.add(hList.getListId());
                        }
                        adapter.notifyDataSetChanged();
                    }
                });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinner_position = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editTextDesc = findViewById(R.id.edittext_desc);
        findViewById(R.id.button_save).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_save:
                final String desc = editTextDesc.getText().toString().trim();
                final String list_id = hotspot_list_id.get(spinner_position);

                CollectionReference hotspotCollection = db.collection("hotspots");

                Hotspot hotspot = new Hotspot(
                        content,
                        Float.parseFloat(lat),
                        Float.parseFloat(lon),
                        desc,
                        list_id
                        );

                hotspotCollection.add(hotspot)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference hotspotDocRef) {
                                Toast.makeText(SaveHotspotActivity.this, "Hotspot saved", Toast.LENGTH_LONG).show();
                                DocumentReference listDocRef = db.collection("hotspot lists").document(list_id);

                                HotspotList hList = list.get(spinner_position).toObject(HotspotList.class);
                                hList.setListId(list_id);
                                hList.setUser_id(mAuth.getCurrentUser().getUid());
                                hList.addHotspot(hotspotDocRef.getId());

                                listDocRef.set(hList);

                                Intent intent = new Intent(SaveHotspotActivity.this, PhotoListActivity.class);
                                intent.putExtra("content", content);
                                intent.putExtra("latitude", lat);
                                intent.putExtra("longitude", lon);
                                startActivity(intent);
                                }
                            })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(SaveHotspotActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                break;
        }
    }
}
