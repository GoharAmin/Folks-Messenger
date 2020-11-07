package codegradients.com.chatapp.Activities.AuthenticationScreens;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.wang.avi.AVLoadingIndicatorView;

import codegradients.com.chatapp.Activities.MainActivity;
import codegradients.com.chatapp.R;

public class LoginActivity extends AppCompatActivity {
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    FirebaseAuth auth;
    private String verificationCode;

    EditText numberEdit;
    Button loginBtn;
    private static String TAG = LoginActivity.class.getName();
    CountryCodePicker ccp;

    AVLoadingIndicatorView avi;

    //setting and getting main activity
    private static LoginActivity loginActivity;

    public static LoginActivity getLoginActivity() {
        return loginActivity;
    }

    private static void setLoginActivity(LoginActivity loginActivity) {
        LoginActivity.loginActivity = loginActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        LoginActivity.setLoginActivity(this);
        initViews();
        //startFireBaseLogin();

    }

    String number = "";

    private void initViews() {
        ccp = findViewById(R.id.ccp);
        avi = findViewById(R.id.avi);
        avi.setVisibility(View.GONE);
        loginBtn = findViewById(R.id.continue_btn_login);
        numberEdit = findViewById(R.id.number_edit_login);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (numberEdit.getText().toString().isEmpty()) {
                    numberEdit.setError("Enter Your Number First");
                    return;
                }

                avi.setVisibility(View.VISIBLE);

                String text = ccp.getSelectedCountryCodeWithPlus();

                number = text + numberEdit.getText().toString();
                number = number.replaceAll("[\\[\\](){}]", "");

                avi.setVisibility(View.GONE);
                startActivity(new Intent(LoginActivity.this, OTPVerificationActivity.class).putExtra("Number", number));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }
}
