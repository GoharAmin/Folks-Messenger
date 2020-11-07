package codegradients.com.chatapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import codegradients.com.chatapp.Models.CallRecordModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.CallRecordAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class CallsFragment extends Fragment {

    List<CallRecordModel> callRecordModelList = new ArrayList<>();
    RecyclerView callsRecycler;
    CallRecordAdapter callAdapter;
    AVLoadingIndicatorView avi;

    TextView noCallsText;

    public CallsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_calls, container, false);

        callsRecycler = view.findViewById(R.id.contactsCallsRecycler);
        callAdapter = new CallRecordAdapter(callRecordModelList, getContext());
        callsRecycler.setAdapter(callAdapter);
        callsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        avi = view.findViewById(R.id.avi);

        noCallsText = view.findViewById(R.id.noCallsRecordText);

        getData();

        return view;
    }

    private void getData(){
        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("callsRecords");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                callRecordModelList.clear();

                for (DataSnapshot d: dataSnapshot.getChildren()){

                    String callerId = d.child("callerId").getValue(String.class);
                    String calleeId = d.child("calleeId").getValue(String.class);
                    String callPlacingTime = d.child("callPlacingTime").getValue(String.class);
                    boolean isVideo = d.child("isVideo").getValue(Boolean.class);
                    String callId = d.getKey();

                    if (calleeId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) || callerId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        String callStartingTime = "0";

                        if (d.hasChild("callStartingTime")){
                            callStartingTime = d.child("callStartingTime").getValue(String.class);
                        }

                        String endingTime = "0";

                        if (d.hasChild("callEndingTime")){
                            endingTime = d.child("callEndingTime").getValue(String.class);
                        }

                        CallRecordModel model = new CallRecordModel(callId, callerId, calleeId, callPlacingTime, callStartingTime, endingTime, isVideo);
                        callRecordModelList.add(model);
                    }
                }

                Collections.sort(callRecordModelList, new Comparator<CallRecordModel>() {
                    @Override
                    public int compare(CallRecordModel callRecordModel, CallRecordModel t1) {
                        return t1.getPlacingTime().compareTo(callRecordModel.getPlacingTime());
                    }
                });

                avi.setVisibility(View.GONE);
                callAdapter.notifyDataSetChanged();

                //Toast.makeText(getContext(), "SIZE: " + callRecordModelList.size(), Toast.LENGTH_SHORT).show();

                if (callRecordModelList.size() == 0){
                    noCallsText.setVisibility(View.VISIBLE);
                } else {
                    noCallsText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
