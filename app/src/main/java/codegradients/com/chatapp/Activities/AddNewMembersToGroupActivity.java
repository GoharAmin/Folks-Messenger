package codegradients.com.chatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Models.ContactModel;
import codegradients.com.chatapp.Models.GroupMessageModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.NewMembersToAddAdapter;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class AddNewMembersToGroupActivity extends AppCompatActivity {

    ImageView backBtn;
    Button addBtn;

    ArrayList<String> membersIds = new ArrayList<>();

    public static List<ContactModel> newMembers = new ArrayList<>();

    List<ContactModel> list;
    List<ContactModel> Contacts = new ArrayList<>();

    RecyclerView newUsersRecycler;
    NewMembersToAddAdapter addAdapter;

    String groupId, groupName;

    //Firebase Instances
    FirebaseAuth mAuth;

    boolean first = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_members_to_group);

        mAuth = FirebaseAuth.getInstance();

        membersIds = getIntent().getExtras().getStringArrayList("Members");
        groupId = getIntent().getStringExtra("GroupId");
        groupName = getIntent().getStringExtra("GroupName");

        list = new ArrayList<>();
        newUsersRecycler = findViewById(R.id.new_users_recycler);
        addAdapter = new NewMembersToAddAdapter(list, this);
        newUsersRecycler.setAdapter(addAdapter);
        newUsersRecycler.setLayoutManager(new LinearLayoutManager(this));

        backBtn = findViewById(R.id.back_btn_add_new);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addBtn = findViewById(R.id.add_btn_new_members);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newMembers.size() == 0) {
                    MDToast.makeText(AddNewMembersToGroupActivity.this, "Select Some Memebers first.", MDToast.LENGTH_LONG, MDToast.TYPE_ERROR).show();
                    return;
                }

                int size = newMembers.size();

                List<ContactModel> reserve = new ArrayList<>(newMembers);

                try {
                    for (int i = 0; i < size; i++) {
                        final ContactModel model = reserve.get(i);
                        DatabaseReference mDatabaseForAddinginGroup = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("users");
                        mDatabaseForAddinginGroup.child(model.getUserId()).setValue("user");

                        //final String messag = HelperClass.encrypt("groupJoining");

                        String adderName = getSharedPreferences("chatAppSP", MODE_PRIVATE).getString("myName", "");

                        String key = String.valueOf(System.currentTimeMillis());
                        GroupMessageModel modelGroup = new GroupMessageModel(adderName, groupId, model.getUserId(), key, HelperClass.encrypt(model.getUserName()), String.valueOf(System.currentTimeMillis()), "groupJoining");
                        FirebaseDatabase.getInstance().getReference().child("Groups").child(groupId).child("Messages").child(key).setValue(modelGroup);

                        try {
                            HelperClass.sendNotificationOfGroupAdding(AddNewMembersToGroupActivity.this, groupId, groupName, model.getUserName(), model.getUserToken(), adderName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        newMembers.remove(model);

                        if (newMembers.size() == 0) {
                            Toast.makeText(AddNewMembersToGroupActivity.this, "New Members Added", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

        getContacts();
    }

    private void getContacts(){

        try {
            for (ContactModel model: MainActivity.Live_Contacts){

                if (!mAuth.getCurrentUser().getUid().equals(model.getUserId())) {
                    if (!membersIds.contains(model.getUserId())) {
                        list.add(model);
                    }
                }
            }

            addAdapter.notifyDataSetChanged();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
