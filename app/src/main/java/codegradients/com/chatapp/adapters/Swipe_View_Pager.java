package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

import codegradients.com.chatapp.Activities.ImageViewingActivity;
import codegradients.com.chatapp.Activities.PostDetailActivity;
import codegradients.com.chatapp.Activities.VideoPlayingActivity;
import codegradients.com.chatapp.Models.DataModel;
import codegradients.com.chatapp.R;


public class Swipe_View_Pager extends PagerAdapter {

    private List<DataModel> Image_Data = new ArrayList<>();
    Context mContext;
    LayoutInflater Inflater;
    public Swipe_View_Pager(Context activity, List<DataModel> image_Data) {
        Image_Data = image_Data;
        this.mContext = activity;
    }

    @Override
    public int getCount() {
        return Image_Data.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        Inflater=LayoutInflater.from(mContext);
        View Layout_Connection=Inflater.inflate(R.layout.view_pager_container,container,false);

        ImageView Image_View;
        final AVLoadingIndicatorView P_Bar;
        Image_View =Layout_Connection.findViewById(R.id.Image_Box_View_Id);
        P_Bar=Layout_Connection.findViewById(R.id.avi);

        ImageView playIcon = Layout_Connection.findViewById(R.id.playIcon);
        if (Image_Data.get(position).getType().equals("image")){
            playIcon.setVisibility(View.GONE);
        } else {
            playIcon.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext).load(Image_Data.get(position).getLink()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                P_Bar.setVisibility(View.INVISIBLE);
                return false;
            }
        }).into(Image_View);

        if (Image_Data.get(position).getType().equals("image")){
            Layout_Connection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, ImageViewingActivity.class).putExtra("URL",Image_Data.get(position).getLink()));
                }
            });
        } else {
            Layout_Connection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.startActivity(new Intent(mContext, VideoPlayingActivity.class).putExtra("videoURL",Image_Data.get(position).getLink()).putExtra("headingText", PostDetailActivity.selectedModel.getPosterName()));
                }
            });
        }
        container.addView(Layout_Connection);
        return Layout_Connection;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ((ViewPager)container).removeView((View)object);
    }
}
