package com.example.no0ne.ears;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class JobDetailsActivity extends AppCompatActivity {

    private static final int FILE_SELECT_CODE = 0;

    private Button mFileChooserButton;
    private Button mApplyNowButton;

    private FirebaseUser mCurrentUser;
    private StorageReference mFileReference;
    private DatabaseReference mUserReference;

    Uri fileUri;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_details);

        mFileChooserButton = (Button) findViewById(R.id.button_file_chooser);
        mApplyNowButton = (Button) findViewById(R.id.button_apply_now);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mFileReference = FirebaseStorage.getInstance().getReference();
        String uId = mCurrentUser.getUid();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);

        dialog = new ProgressDialog(JobDetailsActivity.this);

        mFileChooserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                intent.addCategory(Intent.CATEGORY_OPENABLE);
//                intent.setType("*/*");
//                startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_SELECT_CODE);
            }
        });

        mApplyNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("Application Sending...");
                dialog.show();
                storingFile(fileUri);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            Toast.makeText(JobDetailsActivity.this, "File Selected!", Toast.LENGTH_SHORT).show();
            fileUri = data.getData();
//            storingFile(fileUri);
        }
    }

    private void storingFile(Uri uri) {
        String uId = mCurrentUser.getUid();
        StorageReference reference = mFileReference.child("files").child(uId);

        reference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    String fileUrl = task.getResult().getDownloadUrl().toString();

                    Map map = new HashMap<String, String>();
                    map.put("file", fileUrl);

                    mUserReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Toast.makeText(JobDetailsActivity.this, "Application Received!", Toast.LENGTH_SHORT)
                                        .show();
                            } else {
                                Toast.makeText(JobDetailsActivity.this, "Something Went Wrong!", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(JobDetailsActivity.this, "Storage Reference Problem!" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
