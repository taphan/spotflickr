package com.cs550.teama.spotflickr.activity.hotspot;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs550.teama.spotflickr.R;
import com.cs550.teama.spotflickr.activity.PhotoListActivity;
import com.cs550.teama.spotflickr.activity.auth.ChangePasswordActivity;
import com.cs550.teama.spotflickr.activity.auth.LoginActivity;
import com.cs550.teama.spotflickr.activity.user.UserProfileFragmentActivity;
import com.cs550.teama.spotflickr.model.HotspotList;
import com.cs550.teama.spotflickr.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class CreateHotspotListActivity extends AppCompatActivity implements View.OnClickListener {

    private String content;
    private String place_id;
    private String lat;
    private String lon;

    private EditText editTextName;
    private EditText editTextDesc;
    private List<String> hotspotListNames;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DocumentReference userDocRef = db.collection("users").document(mAuth.getCurrentUser().getUid());
    private User current_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hotspot_list);

        Bundle extras = getIntent().getExtras();
        content = extras.getString("content");
        place_id = extras.getString("place_id");
        lat = extras.getString("latitude");
        lon = extras.getString("longitude");

        hotspotListNames = new ArrayList<String>();

        db.collection("hotspot lists").whereEqualTo("user_id", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                        for (DocumentSnapshot d : list) {
                            HotspotList hList = d.toObject(HotspotList.class);
                            hotspotListNames.add(hList.getName());
                        }
                    }
                });

        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    current_user = documentSnapshot.toObject(User.class);
                }
                else
                    Toast.makeText(getApplicationContext(), "Document not found", Toast.LENGTH_SHORT).show();
            }
        });

        editTextName = findViewById(R.id.edittext_name);
        editTextDesc = findViewById(R.id.edittext_desc);

        findViewById(R.id.button_save).setOnClickListener(this);
    }

    private boolean hasValidationErrors(String name, String desc) {
        if (name.isEmpty()) {
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return true;
        }

        for (int i = 0; i < hotspotListNames.size(); i++) {
            if (name.equals(hotspotListNames.get(i))) {
                editTextName.setError("Same name already exists");
                editTextName.requestFocus();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_save:
                final String name = editTextName.getText().toString().trim();
                final String desc = editTextDesc.getText().toString().trim();

                if (!hasValidationErrors(name, desc)) {
                    CollectionReference dbProducts = db.collection("hotspot lists");

                    List<String> hotspot = new ArrayList<String>();
                    HotspotList hotspotList = new HotspotList(
                            name,
                            desc,
                            mAuth.getCurrentUser().getUid(),
                            hotspot
                    );

                    dbProducts.add(hotspotList)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference listDocRef) {
                                    Toast.makeText(CreateHotspotListActivity.this, "Hotspot List Added", Toast.LENGTH_LONG).show();
                                    current_user.addHotspotList(listDocRef.getId());
                                    userDocRef.set(current_user);
                                    Intent intent = new Intent(CreateHotspotListActivity.this, SaveHotspotActivity.class);
                                    intent.putExtra("content", content);
                                    intent.putExtra("place_id", place_id);
                                    intent.putExtra("latitude", lat);
                                    intent.putExtra("longitude", lon);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(CreateHotspotListActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
                break;
        }
    }
}
