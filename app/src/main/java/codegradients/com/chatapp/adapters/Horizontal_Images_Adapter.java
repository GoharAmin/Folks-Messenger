package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import codegradients.com.chatapp.Models.DataChoosingPostModel;
import codegradients.com.chatapp.R;


public class Horizontal_Images_Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mContext;
    private ArrayList<DataChoosingPostModel> Image_Data = new ArrayList<>();


    public Horizontal_Images_Adapter(Context context, ArrayList<DataChoosingPostModel> Items)
    {
        this.mContext =context;
        this.Image_Data=Items;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater Inflater= LayoutInflater.from(mContext);
        View Row=Inflater.inflate(R.layout.horizontal_image_box,parent,false);
        Item New_item=new Item(Row);
        return New_item;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position)
    {


//        Picasso.with(mContext).load(Image_Data.get(position)).into(((Item)holder).Image_View, new Callback() {
//            @Override
//            public void onSuccess() {
//                ((Item)holder).P_Bar.setVisibility(View.INVISIBLE);
//            }
//            @Override
//            public void onError() {
//                ((Item)holder).P_Bar.setVisibility(View.INVISIBLE);
//                ((Item)holder).Image_View.setImageResource(R.drawable.ic_person);
//            }
//        });

        ((Item)holder).removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image_Data.remove(position);
                notifyItemRemoved(position);
            }
        });

        if (Image_Data.get(position).getType().contains("image")){
            ((Item)holder).playBtn.setVisibility(View.GONE);
        } else {
            ((Item)holder).playBtn.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext).load(Image_Data.get(position).getData()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                ((Item)holder).P_Bar.setVisibility(View.INVISIBLE);
                ((Item)holder).Image_View.setImageResource(R.drawable.ic_smartphone);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                ((Item)holder).P_Bar.setVisibility(View.INVISIBLE);
                return false;
            }
        }).into(((Item)holder).Image_View);

    }

    @Override
    public int getItemCount() {
        return Image_Data.size();
    }

    public class Item extends RecyclerView.ViewHolder {
        private ImageView Image_View, playBtn, removeBtn;
        private ProgressBar P_Bar;

        public Item(View itemView) {
            super(itemView);
            Image_View =itemView.findViewById(R.id.Image_Box_View_Id);
            P_Bar=itemView.findViewById(R.id.avi);
            playBtn = itemView.findViewById(R.id.playBtn);
            removeBtn = itemView.findViewById(R.id.crossBtn);
        }
    }


}
