package codegradients.com.chatapp.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import codegradients.com.chatapp.Models.StatusInfoModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.MyStatusesRecAdapter;

public class MyStatusActivity extends AppCompatActivity {


    Toolbar mToolbarMystatus;
    @BindView(R.id.recview_mystatus)
    RecyclerView mRecviewMystatus;
    public static String KEY_STATUS_LIST = "status_list";
    private ArrayList<StatusInfoModel> mStatusInfoModelArrayList = new ArrayList<>();
    private static String TAG = MyStatusActivity.class.getName();
    private MyStatusesRecAdapter mRecAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_status);
        ButterKnife.bind(this);

        findViewById(R.id.backIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        gettingIntent();
        settingRecyclerView();

    }

    private void settingRecyclerView() {
        mRecAdapter = new MyStatusesRecAdapter(getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecviewMystatus.setLayoutManager(mLayoutManager);
        mRecviewMystatus.setItemAnimator(new DefaultItemAnimator());
        mRecviewMystatus.setAdapter(mRecAdapter);
        mRecAdapter.setData(mStatusInfoModelArrayList);

    }

    private void gettingIntent() {
        mStatusInfoModelArrayList = (ArrayList<StatusInfoModel>) getIntent().getSerializableExtra(KEY_STATUS_LIST);
        Log.i(TAG,"SIze"+mStatusInfoModelArrayList.size());
    }


}
