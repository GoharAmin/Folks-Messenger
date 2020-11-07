package codegradients.com.chatapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import codegradients.com.chatapp.Activities.ShowStatusActivity;
import codegradients.com.chatapp.Models.StatusInfoModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class MyStatusesRecAdapter extends RecyclerView.Adapter<MyStatusesRecAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<StatusInfoModel> mInfoArrayList = new ArrayList<>();

    public MyStatusesRecAdapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_rec_my_status, parent, false);

        return new MyStatusesRecAdapter.MyViewHolder(itemView);

    }

    public void setData(ArrayList<StatusInfoModel> arrayList) {
        mInfoArrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    StatusInfoModel model = mInfoArrayList.get(position);
        Glide.with(mContext).load(model.getUrl()).into(holder.mCircularImageView);
        holder.mTextViewViews.setText(model.getViewModelArrayList().size()+" Views");
        long t = Long.parseLong(model.getStatusUploadDate());
        String date = HelperClass.formateDate(t);
        String time = HelperClass.formateTime(t);

        holder.mTextViewTime.setText(date+"  "+time);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<StatusInfoModel> arrayList = new ArrayList<>();
                arrayList.add(model);
                Intent intent = new Intent(mContext, ShowStatusActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ShowStatusActivity.KEY_LIST,arrayList);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mInfoArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
         CircularImageView mCircularImageView;
        TextView mTextViewViews,mTextViewTime;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mCircularImageView = itemView.findViewById(R.id.img_mystaus);
            mTextViewViews = itemView.findViewById(R.id.txtView_views);
            mTextViewTime = itemView.findViewById(R.id.txtView_time);
        }
    }
}
