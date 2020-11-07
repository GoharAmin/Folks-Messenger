package codegradients.com.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import codegradients.com.chatapp.Models.CommentModel;
import codegradients.com.chatapp.Models.DataModel;
import codegradients.com.chatapp.Models.PostsModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.adapters.PostCommentAdapter;
import codegradients.com.chatapp.adapters.Swipe_View_Pager;

public class PostDetailActivity extends AppCompatActivity {

    public static PostsModel selectedModel = new PostsModel();

    TextView dogNameText, dogDescription, dogDateTime;

    public static TextView numberOfImage;

    private ViewPager Images_View;

    TextView likeNumberText, commentNumberText;
    ImageView likeImage, commentImage;

    List<String> likedBy = new ArrayList<>();
    List<CommentModel> commentModelList = new ArrayList<>();
    RecyclerView commentsRecycler;
    PostCommentAdapter commentAdapter;

    CircularProgressBar commentsProgressBar;

    EditText commentEdit;
    ImageButton commendPostBtn;

    TextView noCommentsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        selectedModel = MainActivity.getMainActivity().selectedModelPostForDetail;

        Images_View = findViewById(R.id.Image_Recycle_ID);
        dogNameText = findViewById(R.id.dogName);
        dogDescription = findViewById(R.id.dogDescription);
        dogDateTime = findViewById(R.id.dogDateTime);
        likeImage = findViewById(R.id.likeIconPostCard);
        likeNumberText = findViewById(R.id.likeNumberPostCard);
        commentImage = findViewById(R.id.commentIconPostCard);
        commentNumberText = findViewById(R.id.commentNumberPostCard);
        commentsProgressBar = findViewById(R.id.commentsProgress);
        commentsRecycler = findViewById(R.id.commentsRecycler);
        commentEdit = findViewById(R.id.commentsEdit);
        commendPostBtn = findViewById(R.id.postCommentBtn);
        noCommentsText = findViewById(R.id.noCommentsText);
        noCommentsText.setVisibility(View.GONE);

        commentAdapter = new PostCommentAdapter(commentModelList, this);
        commentsRecycler.setAdapter(commentAdapter);
        commentsRecycler.setLayoutManager(new LinearLayoutManager(this));

        refreshData();

        dogNameText.setText(selectedModel.getPosterName());
        dogDescription.setText(selectedModel.getDescription());

        numberOfImage = findViewById(R.id.numberOfImage);

        if (selectedModel.getData().size() == 0) {
            numberOfImage.setVisibility(View.GONE);
        } else {
            numberOfImage.setVisibility(View.VISIBLE);
        }

        Images_View.setAdapter(new Swipe_View_Pager(PostDetailActivity.this, selectedModel.getData()));

        numberOfImage.setText("" + 1 + "/" + selectedModel.getData().size());

        if (selectedModel.getLikedBy().contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            likeImage.setImageDrawable(getResources().getDrawable(R.drawable.liked_iccc));
        } else {
            likeImage.setImageDrawable(getResources().getDrawable(R.drawable.like_iccc));
        }

        likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Toast.makeText(PostDetailActivity.this, "Size: " + likedBy.size(), Toast.LENGTH_SHORT).show();
                DatabaseReference mDatabaseForLiking = FirebaseDatabase.getInstance().getReference().child("StatusPosts").child(selectedModel.getPostId());
                if (likedBy.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    mDatabaseForLiking.child("likedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                    likeImage.setImageDrawable(getResources().getDrawable(R.drawable.like_iccc));
                } else {
                    mDatabaseForLiking.child("likedBy").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
                    likeImage.setImageDrawable(getResources().getDrawable(R.drawable.liked_iccc));
                }
            }
        });

        if (selectedModel.getLikedBy().size() == 0){
            likeNumberText.setVisibility(View.INVISIBLE);
        } else {
            likeNumberText.setVisibility(View.VISIBLE);
            likeNumberText.setText(String.valueOf(selectedModel.getLikedBy().size()));
        }

        if (selectedModel.getCommentsCount() == 0){
            commentNumberText.setVisibility(View.INVISIBLE);
        } else {
            commentNumberText.setVisibility(View.VISIBLE);
            commentNumberText.setText(String.valueOf(selectedModel.getCommentsCount()));
        }

        Images_View.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                int pos = position + 1;

                numberOfImage.setText("" + pos + "/" + selectedModel.getData().size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        try {
            double currentt = System.currentTimeMillis() - selectedModel.getCreated_at();

            double ttime = currentt / 1000;
            double minuttes  = ttime / 60;

            int ttimestamp = (int) minuttes;

            if (ttimestamp > 60){

                int hours = ttimestamp / 60;

                int minutess = ttimestamp % 60;

                if (hours > 24){

                    long curre = selectedModel.getCreated_at();

                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String dateString = formatter.format(new Date(curre));
                    dogDateTime.setText(dateString);

                } else {
                    dogDateTime.setText(String.valueOf(hours) + " h " + String.valueOf(minutess) + " min ago");
                }

            }else {

                if (ttimestamp == 0){
                    dogDateTime.setText("Just Now");
                } else {
                    dogDateTime.setText(String.valueOf(ttimestamp) + " minutes ago");
                }
            }
        } catch (Exception e){
            Log.v("Errorrr: " , e.getMessage());
        }

        commendPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!commentEdit.getText().toString().isEmpty()){
                    String commentText = commentEdit.getText().toString();

                    DatabaseReference mDatabaseForCommentPosting = FirebaseDatabase.getInstance().getReference().child("StatusPosts").child(selectedModel.getPostId());
                    HashMap Data = new HashMap();
                    Data.put("comment", commentText);
                    Data.put("commenterId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    Data.put("commenterImage", MainActivity.image);
                    Data.put("commenterName", MainActivity.userName);
                    Data.put("commentTime", String.valueOf(System.currentTimeMillis()));

                    commentEdit.setText("");
                    hideKeyboard();

                    mDatabaseForCommentPosting.child("comments").push().updateChildren(Data).addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            MDToast.makeText(PostDetailActivity.this, "Comment Posted", MDToast.LENGTH_LONG, MDToast.TYPE_SUCCESS).show();
                        }
                    });
                }
            }
        });
    }

    private void refreshData(){
        DatabaseReference mDatabaseForGettingData = FirebaseDatabase.getInstance().getReference().child("StatusPosts").child(selectedModel.getPostId());
        mDatabaseForGettingData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot d) {
                try {
                    String postId = d.getKey();
                    String description = d.child("description").getValue(String.class);
                    long created_at = d.child("created_at").getValue(Long.class);
                    String posterId = d.child("posterId").getValue(String.class);
                    String posterImage = d.child("posterImage").getValue(String.class);
                    String posterName = d.child("posterName").getValue(String.class);

                    likedBy.clear();
                    if (d.hasChild("likedBy")){
                        for (DataSnapshot likedSnapshot: d.child("likedBy").getChildren()){
                            String likerId = likedSnapshot.getKey();
                            likedBy.add(likerId);
                        }
                    }

                    if (likedBy.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        likeImage.setImageDrawable(getResources().getDrawable(R.drawable.liked_iccc));
                    } else {
                        likeImage.setImageDrawable(getResources().getDrawable(R.drawable.like_iccc));
                    }

                    List<DataModel> dataModels = new ArrayList<>();
                    for (DataSnapshot data: d.child("Data").getChildren()){

                        String link = data.child("link").getValue(String.class);
                        String type = data.child("type").getValue(String.class);
                        long uploaded_at = data.child("uploaded_at").getValue(Long.class);

                        dataModels.add(new DataModel(link, type, uploaded_at));
                    }

                    dogNameText.setText(posterName);
                    dogDescription.setText(description);

                    commentModelList.clear();
                    if (d.hasChild("comments")){
                        for (DataSnapshot commentDatasnapshot: d.child("comments").getChildren()){
                            String commentId = commentDatasnapshot.getKey();
                            String comment = commentDatasnapshot.child("comment").getValue(String.class);
                            String commenterId = commentDatasnapshot.child("commenterId").getValue(String.class);
                            String commenterName = commentDatasnapshot.child("commenterName").getValue(String.class);
                            String commenterImage = commentDatasnapshot.child("commenterImage").getValue(String.class);
                            String commentTime = commentDatasnapshot.child("commentTime").getValue(String.class);

                            commentModelList.add(new CommentModel(commentId, comment, commentTime, commenterId, commenterName, commenterImage));
                        }
                    }

                    commentsProgressBar.setVisibility(View.GONE);

                    if (commentModelList.size() == 0){
                        noCommentsText.setVisibility(View.VISIBLE);
                    } else {
                        noCommentsText.setVisibility(View.GONE);
                    }

                    commentNumberText.setText(String.valueOf(commentModelList.size()));

                   // Toast.makeText(PostDetailActivity.this, "Size: " + commentModelList.size(), Toast.LENGTH_SHORT).show();

                    commentAdapter.notifyDataSetChanged();

                    if (likedBy.size() == 0){
                        likeNumberText.setVisibility(View.INVISIBLE);
                    } else {
                        likeNumberText.setVisibility(View.VISIBLE);
                        likeNumberText.setText(String.valueOf(likedBy.size()));
                    }

                    if (commentModelList.size() == 0){
                        commentNumberText.setVisibility(View.INVISIBLE);
                    } else {
                        commentNumberText.setVisibility(View.VISIBLE);
                        commentNumberText.setText(String.valueOf(commentModelList.size()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}