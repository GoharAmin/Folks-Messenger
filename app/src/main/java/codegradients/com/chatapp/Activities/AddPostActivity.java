package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;

import codegradients.com.chatapp.Models.DataChoosingPostModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.Horizontal_Images_Adapter;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class AddPostActivity extends AppCompatActivity {

    EditText descriptionEdit;
    private RecyclerView Selected_Images_View;
    private Horizontal_Images_Adapter Images_Adapter;
    private ArrayList<DataChoosingPostModel> Images_List = new ArrayList<>();
    private ArrayList<DataChoosingPostModel> Uploaded_Images_URL = new ArrayList<>();
    private CardView Add_Image;

    Button publishBtn;

    HelperClass helperClass;

    DatabaseReference mDatabaseReferenceForPostingPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        helperClass = new HelperClass(this);

        initViews();
    }

    private void initViews(){

        descriptionEdit = findViewById(R.id.postDescription);
        publishBtn = findViewById(R.id.postBtn);
        Selected_Images_View = findViewById(R.id.Images_View_Recycle);
        Add_Image = findViewById(R.id.P_Add_Image_Id);
        Selected_Images_View.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        Selected_Images_View.setLayoutManager(layoutManager);

        Images_Adapter = new Horizontal_Images_Adapter(AddPostActivity.this, Images_List);
        Selected_Images_View.setAdapter(Images_Adapter);

        Add_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(AddPostActivity.this);
                builder.setTitle("Choose Data Type");

                // add a list
                String[] animals = {"Image", "Video"};
                builder.setItems(animals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // horse
                                CropImage.activity()
                                        //.setAspectRatio(1, 1)
                                        .start(AddPostActivity.this);
                                break;
                            case 1: // cow
                                Intent intent = new Intent();
                                intent.setType("video/mp4");
                                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20);
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(intent, "Select videos"), 1321);
                                break;
                        }
                    }
                });

                // create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });

        publishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(AddPostActivity.this, "Size:" + Images_List.size(), Toast.LENGTH_SHORT).show();
                if (descriptionEdit.getText().toString().isEmpty() && Images_List.size() == 0){
                    Toast.makeText(AddPostActivity.this, "Enter Something to Post", Toast.LENGTH_SHORT).show();
                    return;
                }

                mDatabaseReferenceForPostingPosts = FirebaseDatabase.getInstance().getReference().child("StatusPosts");

                postId = mDatabaseReferenceForPostingPosts.push().getKey();

                helperClass.progressDialogAlert.show();
                if (Images_List.size() > 0) {
                    uploadImagesFirst();
                } else {
                    uploadData();
                }
            }
        });
    }

    String postId = "";

    int i = 0;
    private void uploadImagesFirst() {
        helperClass.textInProgressDialog.setText("Uploading Media");

        for (i = 0; i < Images_List.size(); i++) {
            final StorageReference Submit_Datareference = FirebaseStorage.getInstance().getReference("Posts").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(postId).child("Item No : " + i);
            final int finalI = i;
            String typeee = Images_List.get(i).getType();
            Submit_Datareference.putFile(Uri.parse(Images_List.get(i).getData())).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Submit_Datareference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uploaded_Images_URL.add(new DataChoosingPostModel(uri.toString(), typeee));
                            if (finalI == Images_List.size() - 1) {
                                uploadData();
                            }
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddPostActivity.this, "Error!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void uploadData(){
        helperClass.textInProgressDialog.setText("Pushing Data");

        HashMap Data = new HashMap();
        Data.put("description", descriptionEdit.getText().toString());
        Data.put("created_at", System.currentTimeMillis());
        Data.put("posterId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        Data.put("posterName", MainActivity.userName);
        Data.put("posterImage", MainActivity.image);

        HashMap DataHashMap = new HashMap();

        for (int count = 0; count < Uploaded_Images_URL.size(); count++){
            HashMap positionHashMap = new HashMap();
            positionHashMap.put("link", Uploaded_Images_URL.get(count).getData());
            positionHashMap.put("type", Uploaded_Images_URL.get(count).getType());
            positionHashMap.put("uploaded_at", System.currentTimeMillis());

            DataHashMap.put(String.valueOf(count), positionHashMap);
        }

        Data.put("Data", DataHashMap);

        mDatabaseReferenceForPostingPosts.child(postId).updateChildren(Data).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                helperClass.progressDialogAlert.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Images_List.add(new DataChoosingPostModel(result.getUri().toString(), "image"));
                Images_Adapter.notifyDataSetChanged();
                Selected_Images_View.smoothScrollToPosition(Images_List.size() - 1);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                error.printStackTrace();
            }
        } else if (requestCode == 1321){
            if (resultCode == RESULT_OK){

                Images_List.add(new DataChoosingPostModel(data.getData().toString(), "video"));
                Images_Adapter.notifyDataSetChanged();
                Selected_Images_View.smoothScrollToPosition(Images_List.size() - 1);
            }
        }
    }
}