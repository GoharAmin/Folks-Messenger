package codegradients.com.chatapp.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.wang.avi.AVLoadingIndicatorView;

import codegradients.com.chatapp.R;

public class ImageViewingActivity extends AppCompatActivity {

    private AVLoadingIndicatorView P_Bar;
    String url;

    PhotoView ImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewing);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        url = getIntent().getStringExtra("URL");
        P_Bar=findViewById(R.id.avi);
        ImageView = findViewById(R.id.Image_View_ID);

        Glide.with(ImageViewingActivity.this).load(url).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                P_Bar.setVisibility(View.GONE);
                ImageView.setImageResource(R.drawable.ic_launcher_background);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                P_Bar.setVisibility(View.GONE);
                return false;
            }
        }).into(ImageView);
    }
}
