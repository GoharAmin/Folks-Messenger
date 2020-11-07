package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushTokenRegistrationCallback;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.UserController;
import com.sinch.android.rtc.UserRegistrationCallback;
import com.theartofdev.edmodo.cropper.CropImage;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import codegradients.com.chatapp.Activities.AuthenticationScreens.LoginActivity;
import codegradients.com.chatapp.Activities.SinchScreens.BaseActivity;
import codegradients.com.chatapp.Fragments.CallsFragment;
import codegradients.com.chatapp.Fragments.ChatsFragment;
import codegradients.com.chatapp.Fragments.SocialStatusFragment;
import codegradients.com.chatapp.Fragments.UserProfileFragment;
import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.Models.PostsModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.ViewPagerAdapter;
import codegradients.com.chatapp.helper_classes.HelperClass;
import codegradients.com.chatapp.helper_classes.SinchService;
import codegradients.com.chatapp.sessions.UserSessions;

public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener, SinchService.StartFailedListener, PushTokenRegistrationCallback, UserRegistrationCallback {

    ImageView addBtnMain, optionsBtnMain;

    public static ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    ChatsFragment chatsFragment;
    SocialStatusFragment statusFragment;
    CallsFragment callsFragment;
    UserProfileFragment userProfileFragment;

//    ImageView optionsImg;

    public static String userName = "", about = "", image = "";
    private UserSessions mSessions;

    public static SinchClient sinchClient;
    String[] PERMISSIONS = {};

    int PERMISSION_ALL = 1011;

    //setting and getting main activity
    private static MainActivity mainActivity;

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    private static void setMainActivity(MainActivity mainActivity) {
        MainActivity.mainActivity = mainActivity;
    }

    RelativeLayout callLayoutBot, statusLayoutBot, messageLayoutBot, userLayoutBot;
    ImageView callIconBot, statusIconBot, messageIconBot, userIconBot;
    TextView headingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivity.setMainActivity(this);

        initViews();

        settingListener();
        getCurrentUserInfo();

        markEveryMessageDelivered();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            detectScreenshotTaken();

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("userName")) {

                           // String myNameIs = dataSnapshot.child("userName").getValue(String.class);

                        } else {
                            startActivity(new Intent(MainActivity.this, PersonalInfoTakingActivity.class));
                            finish();
                        }
                    } else {
                        startActivity(new Intent(MainActivity.this, PersonalInfoTakingActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            try {
                DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Device_Token");
                String Token_ID = FirebaseInstanceId.getInstance().getToken();
                mDatabase2.setValue(Token_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String[] PERMISSIONS = {
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                    //android.Manifest.permission.CAMERA
            };

            this.PERMISSIONS = PERMISSIONS;

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            } else {
                initSinchClient();
            }

        }

        deletePreviousStatus();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void initSinchClient() {

        Thread timer = new Thread(){
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSinchServiceInterface().setUsername(FirebaseAuth.getInstance().getCurrentUser().getUid());

                            UserController uc = Sinch.getUserControllerBuilder()
                                    .context(getApplicationContext())
                                    .applicationKey(getString(R.string.sinch_app_key))
                                    .userId(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .environmentHost(getString(R.string.sinch_app_environment))
                                    .build();
                            uc.registerUser(MainActivity.this, MainActivity.this);
                        }
                    });
                }
            }
        };
        timer.start();

        //getSinchServiceInterface().setUsername(FirebaseAuth.getInstance().getCurrentUser().getUid());
//        UserController uc = Sinch.getUserControllerBuilder()
//                .context(getApplicationContext())
//                .applicationKey(getString(R.string.sinch_app_key))
//                .userId(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .environmentHost(getString(R.string.sinch_app_environment))
//                .build();
//        uc.registerUser(this, this);

//        sinchClient = Sinch.getSinchClientBuilder()
//                .context(this)
//                .userId(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .applicationKey(getString(R.string.sinch_app_key))
//                .applicationSecret(getString(R.string.sinch_app_secret))
//                .environmentHost(getString(R.string.sinch_app_environment))
//                .build();
//
//        sinchClient.startListeningOnActiveConnection();
//
//        sinchClient.setSupportManagedPush(true);
//        sinchClient.setSupportCalling(true);
//        sinchClient.start();
//
//        sinchClient.startListeningOnActiveConnection();

    }

    private void settingListener() {
        viewPager.addOnPageChangeListener(this);
    }

    private void initViews() {
        mSessions = new UserSessions(getApplicationContext());
//        optionsImg = findViewById(R.id.moreBtn);
//        optionsImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                PopupMenu popupMenu = new PopupMenu(MainActivity.this, optionsImg);
//
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    public boolean onMenuItemClick(MenuItem item) {
//
//                        switch (item.getItemId()) {
//
//                            case R.id.profile:
//                                startActivity(new Intent(MainActivity.this, UserProfileActivity.class).putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid()));
//                                break;
//                            case R.id.webLogin:
//                                startActivity(new Intent(MainActivity.this, BarcodeScanningForWebLoginActivity.class));
//                                break;
//                        }
//                        return true;
//                    }
//                });
//
//                popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
//                popupMenu.show();
//            }
//        });

        addBtnMain = findViewById(R.id.addBtnMain);
        optionsBtnMain = findViewById(R.id.optionBtnMain);

        callLayoutBot = findViewById(R.id.callLayoutBtn);
        statusLayoutBot = findViewById(R.id.statusLayoutBtn);
        messageLayoutBot = findViewById(R.id.messageLayoutBtn);
        userLayoutBot = findViewById(R.id.personLayoutBtn);

        headingText = findViewById(R.id.headingMain);
        callIconBot = findViewById(R.id.callIconBot);
        statusIconBot = findViewById(R.id.statusIconBot);
        messageIconBot = findViewById(R.id.messageIconBot);
        userIconBot = findViewById(R.id.personIconBot);

        viewPager = findViewById(R.id.view_pager);
       // newChatBtn = findViewById(R.id.newChatBtn);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


//        newChatBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, SelectContactForNewChatActivity.class));
//            }
//        });

        addBtnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viewPager.getCurrentItem() == 1){
                    startActivity(new Intent(MainActivity.this, AddPostActivity.class));
                } else if (viewPager.getCurrentItem() == 2){
                    startActivity(new Intent(MainActivity.this, SelectContactForNewChatActivity.class));
                }
            }
        });

        optionsBtnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //scheduledMessagesMenuItem

                PopupMenu popup = new PopupMenu(MainActivity.this, optionsBtnMain);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.profile_options_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.scheduledMessagesMenuItem:

                                startActivity(new Intent(MainActivity.this, ScheduledMessagesActivity.class));

                                break;

                            case R.id.logoutMenuItem:

                                FirebaseAuth.getInstance().signOut();

                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                finish();

                                break;
                        }

                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        chatsFragment = new ChatsFragment();
        statusFragment = new SocialStatusFragment();
        callsFragment = new CallsFragment();
        userProfileFragment = new UserProfileFragment();

        viewPagerAdapter.addFragment(callsFragment, "CALLS");
        viewPagerAdapter.addFragment(statusFragment, "STATUS");
        viewPagerAdapter.addFragment(chatsFragment, "CHATS");
        viewPagerAdapter.addFragment(userProfileFragment, "PROFILE");

        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(2);
        positionChanged(2);

        callLayoutBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });

        statusLayoutBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });

        messageLayoutBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        userLayoutBot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(3);
            }
        });

        //manageLastSeen();
    }

    ScheduledExecutorService lastSeenScheduler;

    private void manageLastSeen() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            lastSeenScheduler = Executors.newSingleThreadScheduledExecutor();
            lastSeenScheduler.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    Log.i("hello", "world");
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("lastSeen");
                                mDatabase.setValue(String.valueOf(System.currentTimeMillis()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, 1, 20, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //initSinchClient();

        if (lastSeenScheduler != null) {
            lastSeenScheduler.shutdownNow();

            manageLastSeen();
        } else {
            manageLastSeen();
        }

        getUsers();
    }

    private void getUsers() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                //  Get_Contacts();
                getUsersThatAreUsingTheApp();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 11112);
            }

//            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//            scheduler.scheduleAtFixedRate(new Runnable() {
//                public void run() {
//                    Log.i("hello", "world");
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//
//                        }
//                    });
//                }
//            }, 1, 30, TimeUnit.SECONDS);

            //getUsersThatAreUsingTheApp();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1011: {
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : PERMISSIONS) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            permissionsDenied += "\n" + per;
                        }
                    }
                    // Show permissionsDenied

                    if (permissionsDenied.isEmpty()) {
                        initSinchClient();
                    } else {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    }
                    // updateViews();
                }
                return;
            }
        }
    }

    List<ContactModel> Total_Users = new ArrayList<>();

    private void getUsersThatAreUsingTheApp() {
        DatabaseReference mDatabaseForGettingUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mDatabaseForGettingUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Total_Users.clear();

                for (DataSnapshot d : dataSnapshot.getChildren()) {

                    try {
                        String name = HelperClass.decrypt( d.child("userName").getValue(String.class));
                        String about = HelperClass.decrypt(d.child("about").getValue(String.class));
                        String profileImage = HelperClass.decrypt(d.child("profileImage").getValue(String.class));
                        String mobileNumber = HelperClass.decrypt(d.child("mobileNumber").getValue(String.class));

                        String userToken = "";

                        if (d.hasChild("Device_Token")) {
                            userToken = d.child("Device_Token").getValue(String.class);
                        }

                        String id = d.getKey();

                        if (!id.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            ContactModel model = new ContactModel(id, name, mobileNumber, profileImage, about, userToken);
                            Total_Users.add(model);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Get_Contacts(Total_Users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    List<ContactModel> All_Contacts = new ArrayList<>();
    public static List<ContactModel> Live_Contacts = new ArrayList<>();
    List<ContactModel> Non_Live_Contacts = new ArrayList<>();
    List<ContactModel> Temp_Contacts = new ArrayList<>();

    private void Get_Contacts(List<ContactModel> Database_Users) {
        All_Contacts.clear();
        Live_Contacts.clear();
        Non_Live_Contacts.clear();
        Temp_Contacts.clear();

        new Thread(new Runnable() {
            @Override
            public void run() {

                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, "DISPLAY_NAME ASC");

                Log.d("Called---", "Called-----p " + phones.getCount());

                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    ContactModel Data = new ContactModel("", name, phoneNumber, "", "", "");
                    All_Contacts.add(Data);
                }
                phones.close();

                for (ContactModel Small_User : All_Contacts) {

                    for (ContactModel End_User : Database_Users) {

                        if (PhoneNumberUtils.compare(Small_User.getUserNumber(), End_User.getUserNumber())) {

                            int i = 0;
                            for (ContactModel Check_Repetion : Live_Contacts) {
                                if (PhoneNumberUtils.compare(Check_Repetion.getUserNumber(), End_User.getUserNumber())) {
                                    i++;
                                }
                            }
                            if (i == 0) {
                                Live_Contacts.add(End_User);
                            }
                        }
                    }
                }

                for (ContactModel A_User : All_Contacts) {
                    int i = 0;
                    for (ContactModel L_User : Live_Contacts) {
                        if (PhoneNumberUtils.compare(L_User.getUserNumber(), A_User.getUserNumber())) {
                            i++;

                        }

                    }

                    if (i == 0) {
                        int j = 0;
                        for (ContactModel Check_Repetion : Non_Live_Contacts) {
                            if (PhoneNumberUtils.compare(A_User.getUserNumber(), Check_Repetion.getUserNumber())) {
                                j++;
                            }
                        }

                        if (j == 0) {
                            Non_Live_Contacts.add(A_User);
                        }
                    }
                }

                Temp_Contacts = new ArrayList<>(Non_Live_Contacts);

                updateContacts();

                Log.d("Called---", "Called-----pppp " + Live_Contacts.size());
                Log.d("Called---", "Called-----ppppddd " + Non_Live_Contacts.size());
            }
        }).start();
    }

    //Updating Contacts In Database
    private void updateContacts() {
        for (ContactModel model : Live_Contacts) {

            try {
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Contacts");
                mDatabase.child(model.getUserId()).setValue(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else {
            DatabaseReference mDatabaseForCheckingIfDataExists = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            mDatabaseForCheckingIfDataExists.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        boolean nameExists = false, aboutExists = false, imageExists = false, numberExists = false;

                        if (dataSnapshot.hasChild("userName")) {
                            nameExists = true;
                            userName = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));

                            getSharedPreferences("chatAppSP", MODE_PRIVATE).edit().putString("myName", userName).apply();
                        }

                        if (dataSnapshot.hasChild("about")) {
                            aboutExists = true;
                            about = HelperClass.decrypt(dataSnapshot.child("about").getValue(String.class));
                        }

                        if (dataSnapshot.hasChild("profileImage")) {
                            imageExists = true;
                            image = HelperClass.decrypt( dataSnapshot.child("profileImage").getValue(String.class));
                            getSharedPreferences("chatAppSP", MODE_PRIVATE).edit().putString("myImage", image).apply();
                        }

                        if (dataSnapshot.hasChild("mobileNumber")) {
                            numberExists = true;
                        }

                        if (!nameExists || !aboutExists || !imageExists || !numberExists) {
                            startActivity(new Intent(MainActivity.this, PersonalInfoTakingActivity.class));
                            finish();
                        }
                    } else {
                        startActivity(new Intent(MainActivity.this, PersonalInfoTakingActivity.class));
                        finish();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPageSelected(int position) {

        positionChanged(position);

        if (position == 0 || position == 3) {
            addBtnMain.setVisibility(View.GONE);
        } else {
            addBtnMain.setVisibility(View.VISIBLE);
        }

        if (position == 3){
            optionsBtnMain.setVisibility(View.VISIBLE);
        } else {
            optionsBtnMain.setVisibility(View.GONE);
        }
    }

    private void positionChanged(int positionSelected){
        //Handling of Bottom Navigation Icons Changing Based On Changing of Tabs
        if (positionSelected == 0){
            callIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.selected_bot_icon_tint));
            statusIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            messageIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            userIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));

            headingText.setText("Call Records");

        } else if (positionSelected == 1){
            callIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            statusIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.selected_bot_icon_tint));
            messageIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            userIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));

            headingText.setText("Social");
        } else if (positionSelected == 2){
            callIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            statusIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            messageIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.selected_bot_icon_tint));
            userIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            headingText.setText("Chats");
        } else if (positionSelected == 3){
            callIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            statusIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            messageIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.unselected_bot_icon_tint));
            userIconBot.setColorFilter(ContextCompat.getColor(MainActivity.this,
                    R.color.selected_bot_icon_tint));

            headingText.setText("Settings");
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void getCurrentUserInfo() {
        DatabaseReference mDatabaseForGettingInfo = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForGettingInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = "", about = "", number = "";
                if (dataSnapshot.hasChild("userName")) {
                    userName = HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class));
                }

                if (dataSnapshot.hasChild("about")) {
                    about = HelperClass.decrypt(dataSnapshot.child("about").getValue(String.class));

                }

                if (dataSnapshot.hasChild("mobileNumber")) {
                    number = HelperClass.decrypt(dataSnapshot.child("mobileNumber").getValue(String.class));

                }
                mSessions.setUser("", number, userName);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void markEveryMessageDelivered() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Chats");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ddd : dataSnapshot.getChildren()) {
                    for (DataSnapshot dd : ddd.getChildren()) {

                        if (dd.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            for (DataSnapshot d : dd.getChildren()) {

                                String messageStatus = "sent";
                                if (d.hasChild("messageStatus")) {
                                    messageStatus = d.child("messageStatus").getValue(String.class);
                                }

                                if (d.hasChild("message")) {
                                    if (messageStatus.equals("sent")) {
                                        DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("Chats").child(ddd.getKey()).child(dd.getKey()).child(d.getKey());
                                        mData.child("messageStatus").setValue("delivered");
                                        mData.child("deliveredTime").setValue(String.valueOf(System.currentTimeMillis()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deletePreviousStatus() {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("status");
//        mDatabase.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot dd : dataSnapshot.getChildren()) {
//
//                    for (DataSnapshot d : dd.getChildren()) {
//
//                        if (d.exists()) {
//
//                            try {
//                                String status_upload_date = d.child("status_upload_date").getValue(String.class);
//
//                                long before24HoursMillis = System.currentTimeMillis() - 86400000;
//                                long statusTime = Long.parseLong(status_upload_date);
//
//                                if (statusTime < before24HoursMillis) {
//
//                                    deleteThisStatus(dd.getKey(), d.getKey());
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }

    private void deleteThisStatus(String userId, String statusId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("status");
        mDatabase.child(userId).child(statusId).removeValue();
    }

    public static int picEnteredForStatus = 0;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Log.v("HereStatading__", "Here in Activity");

                if (picEnteredForStatus == 1) {
                    Uri imageUri = result.getUri();
                    picEnteredForStatus = 0;
                    uploadImage(imageUri);
                    //mImgViewProfile.setImageURI(imageUri);
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(Uri uri) {
        //For Statuses LIke Whatsapp(Not Using Anymore)
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("status").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String key = databaseReference.push().getKey();

        StorageReference storage = FirebaseStorage.getInstance().getReference().child("status").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(HelperClass.getFileNameFromUri(uri));
        storage.putFile(uri).addOnSuccessListener(taskSnapshot -> {
            storage.getDownloadUrl().addOnSuccessListener(uri1 -> {
                HashMap hashMap = new HashMap();
                hashMap.put("url", uri1.toString());
                hashMap.put("status_upload_date", String.valueOf(System.currentTimeMillis()));
                hashMap.put("status_type", "image");
                hashMap.put("status_duration", "5000");
                databaseReference.child(key).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Image Status Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });


            });

        }).addOnSuccessListener(taskSnapshot -> {
        }).addOnFailureListener(e -> {

        });
    }

    PostsModel selectedModelPostForDetail = new PostsModel();

    public void goToPostDetail(PostsModel model) {
        this.selectedModelPostForDetail = model;

        startActivity(new Intent(MainActivity.this, PostDetailActivity.class));
    }

    private void detectScreenshotTaken() {
        DatabaseReference mDatabaseForDetectingScreenshotTaken = FirebaseDatabase.getInstance().getReference().child("screenshotNotifying").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        mDatabaseForDetectingScreenshotTaken.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot d: dataSnapshot.getChildren()) {
                        String otherPersonId = d.getKey();

                        FirebaseDatabase.getInstance().getReference().child("users").child(otherPersonId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("userName")) {

                                    MDToast.makeText(MainActivity.this, "" + HelperClass.decrypt(dataSnapshot.child("userName").getValue(String.class)) + " has taken a screenshot Of His Chat with You.", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
                                    mDatabaseForDetectingScreenshotTaken.child(otherPersonId).removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        //Toast.makeText(mainActivity, "ON Started Sinch", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void tokenRegistered() {
        if (!getSinchServiceInterface().isStarted()) {

            //Toast.makeText(mainActivity, "Hereee", Toast.LENGTH_SHORT).show();
            
            getSinchServiceInterface().startClient();
        } else {
            //Toast.makeText(mainActivity, "Already Started", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void tokenRegistrationFailed(SinchError sinchError) {
        Toast.makeText(this, "Push token registration failed - incoming calls can't be received!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onCredentialsRequired(ClientRegistration clientRegistration) {
        String toSign = FirebaseAuth.getInstance().getCurrentUser().getUid() + getString(R.string.sinch_app_key) + mSigningSequence + getString(R.string.sinch_app_secret);
        String signature;
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] hash = messageDigest.digest(toSign.getBytes("UTF-8"));
            signature = Base64.encodeToString(hash, Base64.DEFAULT).trim();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
        clientRegistration.register(signature, mSigningSequence++);
    }

    @Override
    public void onUserRegistered() {
        //Toast.makeText(mainActivity, "User Registered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserRegistrationFailed(SinchError sinchError) {
        Toast.makeText(this, "Registration failed!", Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onServiceConnected() {
        getSinchServiceInterface().setStartListener(this);
    }

    private long mSigningSequence = 1;
}
