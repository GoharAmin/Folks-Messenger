package codegradients.com.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;


import java.net.MalformedURLException;
import java.net.URL;

import codegradients.com.chatapp.R;

public class ConferenceCallActivity extends AppCompatActivity {
    EditText etCode;
    Button btnStart,btnAudio;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_conference_call);
        etCode=findViewById(R.id.etCode);
        btnStart=findViewById(R.id.btnStart);
        btnAudio=findViewById(R.id.btnAudio);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            try {
                JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL("https://meet.jit.si"))
                        .setWelcomePageEnabled(false)
                        .build();
                JitsiMeet.setDefaultConferenceOptions(options);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            btnStart.setOnClickListener(view->{
                if(checkInput()) {
                    JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                            .setRoom(etCode.getText().toString())
                            .setWelcomePageEnabled(false)
                            .build();
                    JitsiMeetActivity.launch(ConferenceCallActivity.this, options);
                }

            });
            btnAudio.setOnClickListener(v->{
                if(checkInput()) {
                    JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                            .setRoom(etCode.getText().toString())
                            .setAudioOnly(true)
                            .setVideoMuted(true)
                 //           .setAudioMuted(true)
                            .setWelcomePageEnabled(false)
                            .build();
                    JitsiMeetActivity.launch(ConferenceCallActivity.this, options);
                }
            });

        }else {
            new AlertDialog.Builder(context)
                    .setTitle("Please Upgrade Your Phone")
                    .setMessage("Android Marshmallow is minimum required to get this feature")
                    .create()
                    .show();
            //Toast.makeText(context, "Please Upgrade you Phone to marshmallow for this feature", Toast.LENGTH_SHORT).show();
        }

    }
    private boolean checkInput() {
        if(!TextUtils.isEmpty(etCode.getText())){
            return true;
        }
        Toast.makeText(context, "Please Enter the text to create the Link", Toast.LENGTH_SHORT).show();
        return false;
    }
}