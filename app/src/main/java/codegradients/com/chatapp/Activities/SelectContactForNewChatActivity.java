package codegradients.com.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.ContactsAdapterForNewChat;

public class SelectContactForNewChatActivity extends AppCompatActivity {

    RecyclerView contactsRecycler;
    ContactsAdapterForNewChat adapter;
    List<ContactModel> contactsList = new ArrayList<>();
LinearLayout llConfernce;
    AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contact_for_new_chat);

        initViews();
    }

    private void initViews() {

        Log.v("CalledCNC__", "HEre 1");
llConfernce=findViewById(R.id.llConfernce);
        llConfernce.setOnClickListener(v->{
            startActivity(new Intent(SelectContactForNewChatActivity.this,ConferenceCallActivity.class));
        });
        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.newGroupLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(SelectContactForNewChatActivity.this, CreateNewGroupActivity.class));
            }
        });

        findViewById(R.id.newContactLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                startActivity(intent);
            }
        });

        avi = findViewById(R.id.avi);
        avi.setVisibility(View.VISIBLE);

        contactsRecycler = findViewById(R.id.contactsRecycler);
        adapter = new ContactsAdapterForNewChat(contactsList, this);
        contactsRecycler.setAdapter(adapter);
        contactsRecycler.setLayoutManager(new LinearLayoutManager(this));

        getUsersOfApp();
    }

    private void getUsersOfApp() {

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                Log.i("hello", "world");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.v("CalledCNC__", "HEre size: " + MainActivity.Live_Contacts.size());
                        if (MainActivity.Live_Contacts.size() != 0) {

                            if (MainActivity.Live_Contacts.size() != contactsList.size()) {
                                Log.v("CalledCNC__", "HEre 2");
                                contactsList.clear();

                                avi.setVisibility(View.GONE);

                                contactsList = new ArrayList<>(MainActivity.Live_Contacts);
                                adapter = new ContactsAdapterForNewChat(contactsList, SelectContactForNewChatActivity.this);
                                contactsRecycler.setAdapter(adapter);
                                Log.v("CalledCNC__", "HEre 3");
                            }
                        }

                    }
                });
            }
        }, 1, 3, TimeUnit.SECONDS);
    }
}
