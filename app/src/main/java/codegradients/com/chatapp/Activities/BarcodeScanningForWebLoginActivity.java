package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

import codegradients.com.chatapp.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarcodeScanningForWebLoginActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;

    private static final String FINE_CAMERA = Manifest.permission.CAMERA;

    private Dialog Permission_Required;

    private static final int CAMERA_PERMISSION_CODE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanning_for_web_login);

        Permission_Required();

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
    }

    private void Check_And_Call_Permissions () {
        Log.d("dddd", "Check_And_Call_Permissions() : Checking And Getting Permission");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Permission_Required.dismiss();
            mScannerView.setResultHandler(this);
            mScannerView.startCamera();
            Log.d("ddddd", "Check_And_Call_Permissions(): Permission Allowed");
        } else {
            Permission_Required.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0) {

                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        Log.d("", "onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                Check_And_Call_Permissions();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Check_And_Call_Permissions();

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mScannerView.stopCamera();
        }catch (Exception e){

        }
    }

    @Override
    public void handleResult(Result rawResult) {

        try {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("qr").child(rawResult.getText()).child("user_id");
            mDatabase.setValue(FirebaseAuth.getInstance().getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        finish();
                    } else {
                        Toast.makeText(BarcodeScanningForWebLoginActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        Log.v("Errorrr_", "Error: " + task.getException());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Intent returnIntent = new Intent();
//        returnIntent.putExtra("result", rawResult.getText());
//        setResult(Activity.RESULT_OK, returnIntent);
//        finish();

    }

    private void Permission_Required() {
        Permission_Required = new Dialog(this);
        Permission_Required.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        Permission_Required.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Permission_Required.setContentView(R.layout.permission_required);
        Permission_Required.getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        Permission_Required.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        TextView Permission_View = Permission_Required.findViewById(R.id.allow_permission_id);
        Permission_View.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] permissions = {Manifest.permission.CAMERA};
                ActivityCompat.requestPermissions(BarcodeScanningForWebLoginActivity.this, permissions, CAMERA_PERMISSION_CODE);
            }
        });
        Permission_Required.setCancelable(false);
    }
}
