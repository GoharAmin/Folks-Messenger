package codegradients.com.chatapp.Activities.AuthenticationScreens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.Activities.PersonalInfoTakingActivity;
import codegradients.com.chatapp.R;

public class OTPVerificationActivity extends AppCompatActivity {

    EditText otpEdit;
    Button continueBtn;
    String verificationId = "";
    private static String TAG = OTPVerificationActivity.class.getName();
    public static String KEY_OTP = "otp_key";
    public static String KEY_VER_CODE = "key_ver";
    private FirebaseAuth auth;
    private String mPhoneNumber;

    AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpverification);
        gettingIntent();
        initViews();

        Log.v("Login__", "Number: " + getIntent().getStringExtra("Number"));

        signInWithPhoneNumber();

    }

    private void gettingIntent() {
        mPhoneNumber = getIntent().getStringExtra("Number");
    }

    private void initViews() {
        avi = findViewById(R.id.avi);
        avi.setVisibility(View.GONE);
        auth = FirebaseAuth.getInstance();
        otpEdit = findViewById(R.id.otp_edit_otp);
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        continueBtn = findViewById(R.id.continue_btn_otp);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (otpEdit.getText().toString().isEmpty()) {
                    otpEdit.setError("Enter the OTP first");
                    return;
                }

                if (otpEdit.getText().toString().length() > 6 || otpEdit.getText().toString().length() < 6) {
                    otpEdit.setError("OTP Can only be of 6 letters");
                    return;
                }

                onVerifyClicked();
            }
        });
    }

    private void signInWithPhoneNumber() {

        PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                Log.v("Login__", "Verification Complete");

                //otpEdit.setText(phoneAuthCredential.getSmsCode());
                //onVerifyClicked();

                //Detect the SMS Code
//                String code = phoneAuthCredential.getSmsCode();
//
//                if (code != null) {
//                    try {
//                        //Verify the Code
//                        verifyVerificationCode(code);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(OTPVerificationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.v("Login__", "Error: exception: " + e);
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                OTPVerificationActivity.this.verificationId = verificationId;
                Log.v("Login__", "Code Sent: " + getIntent().getStringExtra("Number"));
            }
        };

        try {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(mPhoneNumber, 60, TimeUnit.SECONDS, this, callbacks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onVerifyClicked() {
        String otp = otpEdit.getText().toString();

        continueBtn.setEnabled(false);
        avi.setVisibility(View.VISIBLE);
        verifyVerificationCode(otp);
    }

    public void verifyVerificationCode(String otp) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            //Sign In The User
            signInWithPhoneAuthCredentials(credential);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void signInWithPhoneAuthCredentials(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                if (dataSnapshot.hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                    startActivity(new Intent(OTPVerificationActivity.this, MainActivity.class));
                                    avi.setVisibility(View.GONE);
                                    continueBtn.setEnabled(true);
                                    finish();
                                } else {
                                    navigateToActivity();
                                }
                            } else {
                                navigateToActivity();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                            avi.setVisibility(View.GONE);
                            continueBtn.setEnabled(true);
                        }
                    });
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        String message = "Invalid Code Entered";
                        avi.setVisibility(View.GONE);
                        continueBtn.setEnabled(true);
                        Toast.makeText(OTPVerificationActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }

    private void navigateToActivity() {
        avi.setVisibility(View.GONE);
        Intent intent = new Intent(this, PersonalInfoTakingActivity.class);
        //intent.putExtra(PersonalInfoActivity.KEY_PHONE_NUMBER, mPhoneNumber);
        startActivity(intent);
        finish();
    }


}
