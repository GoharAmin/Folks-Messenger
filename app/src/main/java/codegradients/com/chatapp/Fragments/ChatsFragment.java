package codegradients.com.chatapp.Fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import codegradients.com.chatapp.Models.InboxModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.InboxAdapter;
import codegradients.com.chatapp.helper_classes.HelperClass;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    RecyclerView contactsChatsRecycler;
    InboxAdapter inboxAdapter;
    List<InboxModel> inboxList = new ArrayList<>();
    List<InboxModel> tempList = new ArrayList<>();
    AVLoadingIndicatorView avi;
    TextView noChatsText;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        avi = view.findViewById(R.id.avi);
        avi.setVisibility(View.VISIBLE);
        noChatsText = view.findViewById(R.id.noChatsText);
        contactsChatsRecycler = view.findViewById(R.id.contactsChatsRecycler);
        inboxAdapter = new InboxAdapter(inboxList, getContext());
        contactsChatsRecycler.setAdapter(inboxAdapter);
        contactsChatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        getChatsIds();
        return view;
    }

    private void getChatsIds(){
        try {
            DatabaseReference mDatabaseForUsers = FirebaseDatabase.getInstance().getReference().child("Chats").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            mDatabaseForUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    inboxList.clear();
                    tempList.clear();

                    inboxAdapter.notifyDataSetChanged();

                    for (DataSnapshot d : dataSnapshot.getChildren()) {

                        final InboxModel model = new InboxModel();

                        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("users").child(d.getKey());
                        mData.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot User_Snap_Shot) {

                                if (!User_Snap_Shot.exists()) {
                                    return;
                                }
                                Log.d("Called", "Called-----" + User_Snap_Shot);
                                Log.d("Called", "Called-----+++" + mData.toString());

                                String name = HelperClass.decrypt(User_Snap_Shot.child("userName").getValue(String.class));
                                Log.d("Called", "Called-----" + name);
                                model.name = name;

                                model.img = HelperClass.decrypt(User_Snap_Shot.child("profileImage").getValue(String.class));
                                model.otherId = User_Snap_Shot.getKey();

                                String to = "";
                                String timestamp = "";
                                String lastMessage = "";

                                for (DataSnapshot dd : d.getChildren()) {

                                    //ReceiverName = dd.child("ReceiverName").getValue().toString();

                                    boolean autoDestructionStatusOfMessage = false;
                                    String autoDestructionTimeOfMessage = "0";
                                    boolean isAutoDeletionTimePassed = false;

                                    if (dd.hasChild("autoDestructionStatus")) {
                                        autoDestructionStatusOfMessage = dd.child("autoDestructionStatus").getValue(Boolean.class);
                                    }

                                    if (dd.hasChild("autoDestructionTime")) {
                                        autoDestructionTimeOfMessage = dd.child("autoDestructionTime").getValue(String.class);
                                    }

                                    if (autoDestructionStatusOfMessage) {
                                        if (!autoDestructionTimeOfMessage.equals("")) {

                                            long timeTillDestruction = Long.parseLong(timestamp) + Long.parseLong(autoDestructionTimeOfMessage);
                                            isAutoDeletionTimePassed = true;
                                        }
                                    }

                                    if (!isAutoDeletionTimePassed) {
                                        String timeStamppp = dd.child("timestamp").getValue(String.class);

                                        if (Long.parseLong(timeStamppp) < System.currentTimeMillis()) {
                                            timestamp = timeStamppp;
                                            lastMessage = HelperClass.decrypt(dd.child("message").getValue(String.class));
                                        }
                                    }
                                }

                                model.lastMessageTimeStamp = timestamp;
                                model.lastMessage = lastMessage;
                                model.type = InboxModel.PERSONEL_TYPE;

                                boolean alreadyExist = false;
                                for (InboxModel mm: inboxList){

                                    if (mm.otherId != null){
                                        if (mm.otherId.equals(model.otherId)){
                                            alreadyExist = true;
                                        }
                                    }
                                }

                                if (!alreadyExist) {
                                    inboxList.add(model);
                                    tempList.add(model);
                                }

                                inboxAdapter.notifyDataSetChanged();

                                arrangeNow();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    getGroups();
                    //Toast.makeText(MainActivity.this, "here", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void getGroups(){

        Log.v("HereCalled__", "Here 0");

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (InboxModel mm: inboxList){
                    if (mm.type == 1){
                        tempList.remove(mm);
                    }
                }

                inboxList = new ArrayList<>(tempList);

                for (DataSnapshot d: dataSnapshot.getChildren()){

                    Log.v("HereCalled__", "Here 1");

                    if (d.hasChild("users")){

                        if (d.child("users").hasChild(FirebaseAuth.getInstance().getCurrentUser().getUid())){

                            Log.v("HereCalled__", "We are in the group");

                            InboxModel model = new InboxModel();
                            model.type = InboxModel.GROUP_TYPE;

                            model.groupId = d.getKey();

                            String image= "";

                            if (d.hasChild("groupImage")){
                                image = d.child("groupImage").getValue(String.class);
                            }

                            model.img = image;

                            String groupName = d.child("groupName").getValue().toString();
                            model.name = groupName;

                            String messageSender = "";
                            String message = "";
                            String timestamp = "";

                            if (d.child("Messages").exists()) {
                                for (DataSnapshot dddd : d.child("Messages").getChildren()) {
                                    if (!HelperClass.decrypt(dddd.child("messageType").getValue(String.class)).equals("groupLeaving") && !HelperClass.decrypt(dddd.child("messageType").getValue(String.class)).equals("groupJoining")) {
                                        messageSender = HelperClass.decrypt(dddd.child("senderName").getValue(String.class));

                                        String timeStamppp = dddd.child("timestamp").getValue(String.class);

                                        if (Long.parseLong(timeStamppp) < System.currentTimeMillis()) {
                                            timestamp = timeStamppp;
                                            message = HelperClass.decrypt(dddd.child("message").getValue(String.class));
                                        }
                                    }
                                }
                            }

                            Log.v("HereCalled__", "Here adding");

                            if (timestamp.isEmpty()){
                                timestamp = d.child("createdOn").getValue(String.class);
                            }

                            model.lastMessageTimeStamp = timestamp;

                            model.lastMessageSender = HelperClass.decrypt(messageSender);
                            model.lastMessage = message;

                            inboxList.add(model);
                            tempList.add(model);
                        } else {
                           // avi.setVisibility(View.GONE);
                        }

                    } else {
                        //avi.setVisibility(View.GONE);
                    }
                }

                arrangeNow();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void arrangeNow(){
        Log.v("HereCalled__Fragment", "Size: " + inboxList.size());


        try {
            Collections.sort(inboxList, new Comparator<InboxModel>() {
                @Override
                public int compare(InboxModel item, InboxModel t1) {
                    String s1 = item.lastMessageTimeStamp.toString();
                    String s2 = t1.lastMessageTimeStamp.toString();
                    return s2.compareToIgnoreCase(s1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Collections.sort(tempList, new Comparator<InboxModel>() {
                @Override
                public int compare(InboxModel item, InboxModel t1) {
                    String s1 = item.lastMessageTimeStamp.toString();
                    String s2 = t1.lastMessageTimeStamp.toString();
                    return s2.compareToIgnoreCase(s1);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (inboxList.size() == 0) {
            noChatsText.setVisibility(View.VISIBLE);
        } else {
            noChatsText.setVisibility(View.GONE);
        }

        inboxAdapter = new InboxAdapter(inboxList, getContext());
        contactsChatsRecycler.setAdapter(inboxAdapter);
        avi.setVisibility(View.GONE);
    }
}
