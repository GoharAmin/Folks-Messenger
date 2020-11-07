package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.valdesekamdem.library.mdtoast.MDToast;

import codegradients.com.chatapp.Activities.AuthenticationScreens.LoginActivity;
import codegradients.com.chatapp.R;

public class Splash extends AppCompatActivity {

    TextView splashText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splashText = findViewById(R.id.splashText);

        Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.mytransition);

        splashText.startAnimation(myAnim);

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
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        if (FirebaseAuth.getInstance().getCurrentUser() == null){
            Thread timer = new Thread(){
                public void run() {
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    finally {
                        goToNextActivity(true);
                    }
                }
            };
            timer.start();
        } else {
            mDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()){
                        if (dataSnapshot.hasChild("userName")){
                            goToNextActivity(true);
                        } else {
                            goToNextActivity(false);
                        }
                    } else {
                        goToNextActivity(false);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    boolean goToLogin = true;

    private void goToNextActivity (boolean goToLogin) {

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            try {
                MDToast.makeText(Splash.this, "Please Grant Permissions To Access to App Completely", MDToast.LENGTH_LONG, MDToast.TYPE_INFO).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        this.goToLogin = goToLogin;

        if (goToLogin) {
            final Intent i = new Intent(Splash.this, LoginActivity.class);
            startActivity(i);
        } else {
            //Means Some Information Is Missing
            startActivity(new Intent(Splash.this, PersonalInfoTakingActivity.class));
        }

        finish();
    }

    String[] PERMISSIONS = {};

    int PERMISSION_ALL = 1011;

    private void askPermissions(){

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
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
                        goToNextActivity(goToLogin);
                    } else {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
                    }
                    // updateViews();
                }
                return;
            }
        }
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
}
