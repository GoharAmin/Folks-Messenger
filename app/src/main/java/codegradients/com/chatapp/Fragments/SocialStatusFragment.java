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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Models.DataModel;
import codegradients.com.chatapp.Models.PostsModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.PostsAdapter;

public class SocialStatusFragment extends Fragment {

    PostsAdapter postsAdapter;
    RecyclerView postsRecycler;
    List<PostsModel> postModels = new ArrayList<>();
    ProgressBar progressBar;

    TextView noPostText;

    public SocialStatusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_social_status, container, false);

        postsRecycler = view.findViewById(R.id.postsRecyclerPuppies);
        noPostText = view.findViewById(R.id.noPostText);
        postsAdapter = new PostsAdapter(postModels, getContext());
        postsRecycler.setAdapter(postsAdapter);
        postsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = view.findViewById(R.id.progressBar);
        getPosts();

        return view;
    }

    private void getPosts(){

        DatabaseReference mDatabaseForGettingLatestPosts = FirebaseDatabase.getInstance().getReference().child("StatusPosts");
        mDatabaseForGettingLatestPosts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postModels.clear();

                for (DataSnapshot d: dataSnapshot.getChildren()){

                    try {
                        String postId = d.getKey();
                        String description = d.child("description").getValue(String.class);
                        long created_at = d.child("created_at").getValue(Long.class);
                        String posterId = d.child("posterId").getValue(String.class);
                        String posterImage = d.child("posterImage").getValue(String.class);
                        String posterName = d.child("posterName").getValue(String.class);

                        List<String> likedBy = new ArrayList<>();

                        if (d.hasChild("likedBy")){
                            for (DataSnapshot likedSnapshot: d.child("likedBy").getChildren()){
                                String likerId = likedSnapshot.getKey();
                                likedBy.add(likerId);
                            }
                        }

                        List<DataModel> dataModels = new ArrayList<>();
                        for (DataSnapshot data: d.child("Data").getChildren()){

                            String link = data.child("link").getValue(String.class);
                            String type = data.child("type").getValue(String.class);
                            long uploaded_at = data.child("uploaded_at").getValue(Long.class);

                            dataModels.add(new DataModel(link, type, uploaded_at));
                        }

                        int commentsCount = 0;

                        if (d.hasChild("comments")){

                            commentsCount = (int) d.child("comments").getChildrenCount();

//                            for (DataSnapshot commentDatasnapshot: d.child("comments").getChildren()){
//                                String commentId = commentDatasnapshot.getKey();
//                                String comment = commentDatasnapshot.child("comment").getValue(String.class);
//                                String commenterId = commentDatasnapshot.child("commenterId").getValue(String.class);
//                                String commenterName = commentDatasnapshot.child("commenterName").getValue(String.class);
//                                String commenterImage = commentDatasnapshot.child("commenterImage").getValue(String.class);
//
//                                commentModelList.add(new CommentModel(commentId, comment, comment, commenterId, commenterName, commenterImage));
//                            }
                        }

                        postModels.add(new PostsModel(postId, description, posterId, posterName, posterImage, created_at, dataModels, likedBy, commentsCount));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.v("DataIsHereHome__", "Size: " + postModels.size());

                progressBar.setVisibility(View.GONE);
                postsAdapter.notifyDataSetChanged();

                if (postModels.size() == 0) {
                    noPostText.setVisibility(View.VISIBLE);
                } else {
                    noPostText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}