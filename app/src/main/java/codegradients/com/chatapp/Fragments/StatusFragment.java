package codegradients.com.chatapp.Fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.Activities.MyStatusActivity;
import codegradients.com.chatapp.Activities.ShowStatusActivity;
import codegradients.com.chatapp.Activities.TextStatusActivity;
import codegradients.com.chatapp.Activities.VideoTrimActivity;
import codegradients.com.chatapp.Models.CompleteStatusInfo;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.Models.StatusInfoModel;
import codegradients.com.chatapp.Models.StatusViewModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.RecUpdateRec_Adapter;
import codegradients.com.chatapp.helper_classes.HelperClass;
import codegradients.com.chatapp.sessions.UserSessions;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusFragment extends Fragment implements View.OnClickListener {
    private FloatingActionButton mButtonTextStatus,mButtonCamStatus,mButtonVideoStatus;
    private static int IMAGE_REQUEST_CODE = 117;
    private static int VIDEO_REQUEST_CODE = 118;
    private static String TAG = StatusFragment.class.getName();
    private CardView mCardViewMyStatus;
    private String mUSerId;
    private List<ContactModel> contactsList = new ArrayList<>();
    private UserSessions mSessions;
    private ArrayList<CompleteStatusInfo> mCompleteStatusInfos = new ArrayList<>();
    private RecyclerView mRecyclerViewRecentStatus;
    private RecUpdateRec_Adapter mRecAdapter;
    private ImageView mImageViewDots;
    private ArrayList<StatusInfoModel> mInfoModelArrayList = new ArrayList<>();
    private TextView mTextViewMyStatusDate;
    public StatusFragment() {
        // Required empty public constructor
    }

    private CircularImageView mImageViewMyStatus;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){
            getUsersOfApp();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_status, container, false);
        initViews(v);
        settingListener();
        getFriendsStatus();
        settingRecentUpdateRecyclerView();
        type = "";
        retrieveMyStatus();
        return v;
    }

    private void getFriendsStatus() {
        if (contactsList!=null) {

        }
    }

    private void initViews(View v) {
        mSessions = new UserSessions(getContext());
        mButtonTextStatus = v.findViewById(R.id.btn_text_status);
        mButtonCamStatus = v.findViewById(R.id.btn_img_status);
        mButtonVideoStatus = v.findViewById(R.id.btn_video_status);
        mCardViewMyStatus = v.findViewById(R.id.cardView_my_status);
        mRecyclerViewRecentStatus = v.findViewById(R.id.recView_recentupdates);
        mImageViewDots = v.findViewById(R.id.imgView_dots);
        mImageViewMyStatus = v.findViewById(R.id.img_status);
        mTextViewMyStatusDate = v.findViewById(R.id.txtView_date);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mUSerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

    }

    private void settingListener() {
        mButtonTextStatus.setOnClickListener(this);
        mButtonCamStatus.setOnClickListener(this);
        mButtonVideoStatus.setOnClickListener(this);
        mCardViewMyStatus.setOnClickListener(this);
        mImageViewDots.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == mButtonTextStatus.getId()) {
            Intent intent = new Intent(getActivity(), TextStatusActivity.class);
            startActivity(intent);

        } else if (id == mButtonCamStatus.getId()) {
//            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            startActivityForResult(intent, IMAGE_REQUEST_CODE);

            //Toast.makeText(getContext(), "Image Picking", Toast.LENGTH_SHORT).show();

            CropImage.activity()
                    //.setAspectRatio(1, 1)
                    .start(getActivity());

            MainActivity.picEnteredForStatus = 1;
        } else if (id == mButtonVideoStatus.getId()) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, VIDEO_REQUEST_CODE);
        } else if (id == mCardViewMyStatus.getId()) {
            type = "m";
            retrieveMyStatus();
        } else if (id == mImageViewDots.getId()) {
            type = "b";
            retrieveMyStatus();
        }
    }

    private String type = "";

    private void retrieveMyStatus() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("status").child(FirebaseAuth.getInstance().getUid());
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        mInfoModelArrayList = new ArrayList<>();
                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            String duration = d.child("status_duration").getValue(String.class);
                            long hours = HelperClass.getNoOfHours(duration);
                            if (hours >= 24) {
                                d.getRef().removeValue();
                                return;
                            }
                            String statuseType = d.child("status_type").getValue(String.class);
                            String statusUploadDate = d.child("status_upload_date").getValue(String.class);

                            long before24HoursMillis = System.currentTimeMillis() - 86400000;
                            long statusTime = Long.parseLong(statusUploadDate);

                            if (statusTime > before24HoursMillis){
                                String url = d.child("url").getValue(String.class);
                                String statusId = d.getKey();
                                ArrayList<StatusViewModel> arrayList = new ArrayList<>();
                                DataSnapshot dataSnapshot1 = d.child("views");
                                for (DataSnapshot d2 : dataSnapshot1.getChildren()) {
                                    Log.i("adaf", "" + d2.getKey());
                                    Log.i("afasf", "" + d2.getValue(String.class));
                                    arrayList.add(new StatusViewModel(d2.getKey(), d2.getValue(String.class)));
                                }
                                StatusInfoModel model = new StatusInfoModel(mUSerId, duration, statuseType, statusUploadDate, url, statusId);
                                model.setViewModelArrayList(arrayList);
                                mInfoModelArrayList.add(model);
                            }
                        }
                        if (mInfoModelArrayList.size()!=0) {
                            Collections.sort(mInfoModelArrayList, new SortByCreationDate());
                            //Collections.reverse(mInfoModelArrayList);
                            Log.i("SortedList", "" + mInfoModelArrayList.get(0).getUrl());
                            try {
                                Glide.with(getContext()).load(mInfoModelArrayList.get(0).getUrl()).into(mImageViewMyStatus);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            long datee = Long.parseLong(mInfoModelArrayList.get(0).getStatusUploadDate());

                            String date = HelperClass.formateDate(datee);
                            String time = HelperClass.formateTime(datee);
                            mTextViewMyStatusDate.setText(date+"  "+time);
                        } else {
                            mTextViewMyStatusDate.setText("");
                        }
                        if (type.equals("m")) {
                            Toast.makeText(getContext(), "M Clidked", Toast.LENGTH_SHORT).show();
                            type = "";
                            if (mInfoModelArrayList.size()!=0) {
                                Intent intent = new Intent(getContext(), ShowStatusActivity.class);
                                intent.putExtra(ShowStatusActivity.KEY_LIST, mInfoModelArrayList);
                                intent.putExtra(ShowStatusActivity.KEY_STATUS_TYPE,"");
                                startActivity(intent);
                            } else {
                                HelperClass.createToast(getContext(),"Please upload status first");
                            }
                        } else if (type.equals("b")) {
                            Toast.makeText(getContext(), "B Clidked", Toast.LENGTH_SHORT).show();
                            type = "";
                            if (mInfoModelArrayList.size()!=0) {
                                Intent intent = new Intent(getContext(), MyStatusActivity.class);
                                intent.putExtra(MyStatusActivity.KEY_STATUS_LIST, mInfoModelArrayList);
                                startActivity(intent);
                            } else {
                                HelperClass.createToast(getContext(),"Please upload the status first");
                            }
                        }
                    //Collections.reverse(popularCriticsList);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }


    class SortByCreationDate implements Comparator<StatusInfoModel> {
        public int compare(StatusInfoModel a, StatusInfoModel b) {
            return Long.compare(Long.parseLong(a.getStatusUploadDate()), Long.parseLong(b.getStatusUploadDate()));
        }
    }

    private void collectAllStatus(Map<String,Object> users) {
        //iterate through each user, ignoring their UID
        int i = 0;
        mInfoModelArrayList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map singleEntity = (Map) entry.getValue();
            //Get phone field and append to list

        }


    }
//    public static String getPath(Context context, Uri uri ) {
//        String result = null;
//        String[] proj = { MediaStore.Images.Media.DATA };
//        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
//        if(cursor != null){
//            if ( cursor.moveToFirst( ) ) {
//                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
//                result = cursor.getString( column_index );
//            }
//            cursor.close( );
//        }
//        if(result == null) {
//            result = "Not found";
//        }
//        return result;
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                Log.v("HereStatading__", "Here in Status");
            }

                if (requestCode == IMAGE_REQUEST_CODE) {
                    //Toast.makeText(getContext(), "Here", Toast.LENGTH_SHORT).show();
//                    Uri selectedImageUri = data.getData();
//                    Log.i(TAG,"Selected image TAG___________"+selectedImageUri.toString());
//                    File compressedImageFile = null;
//                    try {
////                        File file = new File(getPath(getContext(),selectedImageUri));
////                        Log.i(TAG,"Before compression  "+file.length());
////                         compressedImageFile = new Compressor(getContext()).setMaxWidth(640).setMaxHeight(480).setQuality(50).setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
////                                Environment.DIRECTORY_PICTURES).getAbsolutePath()).compressToFile(file);
////                        Log.i(TAG,"After compression  "+compressedImageFile.length());
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Log.i(TAG,"Exception_______"+e.getMessage());
//                    }
                    //uploadImage();
                } else if (requestCode == VIDEO_REQUEST_CODE) {
                    Uri selectedVideo = data.getData();
                    if (selectedVideo!=null) {
                        Log.i(TAG, "Selected video uri________" + selectedVideo.toString());
                        Intent intent = new Intent(getActivity(), VideoTrimActivity.class);
                        intent.putExtra(VideoTrimActivity.KEY_URI, selectedVideo.toString());
                        startActivity(intent);
                    }
                }

        }

    }

    private void uploadImage(Uri uri) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(mUSerId);
        String key = databaseReference.push().getKey();

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("status").child(mUSerId).child(HelperClass.getFileNameFromUri(uri));
        storage.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storage.getDownloadUrl().addOnSuccessListener(uri1 -> {
                HashMap hashMap = new HashMap();
                hashMap.put("url",uri1.toString());
                hashMap.put("status_upload_date",String.valueOf(System.currentTimeMillis()));
                hashMap.put("status_type","image");
                hashMap.put("status_duration","5000");
                databaseReference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                });


            });

        }).addOnSuccessListener(taskSnapshot ->{
        }).addOnFailureListener(e ->{

        });

    }

    private void getUsersOfApp() {

//        Log.v("TestingScheduler", "Here Running");
//        if (MainActivity.Live_Contacts.size() != 0) {
//
//            Log.v("TestingScheduler", "Main Activity List size is not zero");
//
//            if (MainActivity.Live_Contacts.size() != contactsList.size()) {
//
//                Log.v("TestingScheduler", "Lists size was not same");
//                Log.v("CalledCNC__", "HEre 2");
//                contactsList.clear();
//
//                contactsList = new ArrayList<>(MainActivity.Live_Contacts);
//                int i=0;
//                for (ContactModel contactModel : contactsList) {
//                    Log.i("contacts__",""+contactModel.getUserName());
//                    retrieveStatus(contactModel.getUserId(),contactModel.getUserName(),i);
//                    i++;
//                }
//                Log.v("after3", "HEre 3"+"arraylist size"+mCompleteStatusInfos.size());
//            }
//        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.v("TestingScheduler", "Here 0");
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {

                        Log.v("TestingScheduler", "Here Running");
                        if (MainActivity.Live_Contacts.size() != 0) {

                            Log.v("TestingScheduler", "Main Activity List size is not zero");

                            if (MainActivity.Live_Contacts.size() != contactsList.size()) {

                                Log.v("TestingScheduler", "Lists size was not same");
                                Log.v("CalledCNC__", "HEre 2");
                                contactsList.clear();

                                contactsList = new ArrayList<>(MainActivity.Live_Contacts);
                                int i=0;
                                for (ContactModel contactModel : contactsList) {
                                    Log.i("contacts__",""+contactModel.getUserName());
                                    retrieveStatus(contactModel.getUserId(),contactModel.getUserName(),i);
                                    i++;
                                }
                                Log.v("after3", "HEre 3"+"arraylist size"+mCompleteStatusInfos.size());

                                scheduler.shutdownNow();
                            } else {
                                scheduler.shutdownNow();
                            }
                        }

                    }
                });
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    private void settingRecentUpdateRecyclerView() {
        mRecAdapter = new RecUpdateRec_Adapter(getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerViewRecentStatus.setLayoutManager(mLayoutManager);
        mRecyclerViewRecentStatus.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewRecentStatus.setAdapter(mRecAdapter);


    }

    public void retrieveStatus(String userId, String userName, int i) {
        ArrayList<StatusInfoModel> arrayList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("status").child(userId);
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                            if (dataSnapshot!=null)
                            {
                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                    String duration = d.child("status_duration").getValue(String.class);
                                    String statusType = d.child("status_type").getValue(String.class);
                                    String status_upload_date = d.child("status_upload_date").getValue(String.class);

                                    long before24HoursMillis = System.currentTimeMillis() - 86400000;
                                    long statusTime = Long.parseLong(status_upload_date);

                                    if (statusTime > before24HoursMillis){
                                        String url = d.child("url").getValue(String.class);
                                        String key = d.getKey();
                                        arrayList.add(new StatusInfoModel(userId,duration,statusType,status_upload_date,url,key));
                                    }
                                }
                                if (arrayList.size()!=0) {
                                    mCompleteStatusInfos.add(new CompleteStatusInfo(userName, arrayList));
                                }
                                if (contactsList.size()-1 == i) {
                                    mRecAdapter.setData(mCompleteStatusInfos);
                                }
                            }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }


}
