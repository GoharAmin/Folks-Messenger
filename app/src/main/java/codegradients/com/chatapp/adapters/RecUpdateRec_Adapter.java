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
import codegradients.com.chatapp.Models.CompleteStatusInfo;
import codegradients.com.chatapp.Models.StatusInfoModel;
import codegradients.com.chatapp.R;
import codegradients.com.chatapp.helper_classes.HelperClass;

public class RecUpdateRec_Adapter extends RecyclerView.Adapter<RecUpdateRec_Adapter.MyViewHolder> {
    private Context mContext;
    private ArrayList<CompleteStatusInfo> mInfoArrayList = new ArrayList<>();


    public RecUpdateRec_Adapter(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.item_rec_recent_status, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CompleteStatusInfo model = mInfoArrayList.get(position);
        holder.mTextViewName.setText(model.getName());
        StatusInfoModel model1 = model.getStatusInfoModels().get(0);
        Glide.with(mContext).load(model1.getUrl()).into(holder.mImageView);
        long d = Long.parseLong(model1.getStatusUploadDate());
        String date = HelperClass.formateDate(d);
        String time = HelperClass.formateDate(d);
        holder.mTextViewDate.setText(date+"  "+time);
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ShowStatusActivity.class);
            intent.putExtra(ShowStatusActivity.KEY_STATUS_TYPE,"other");
            intent.putExtra(ShowStatusActivity.KEY_LIST,mInfoArrayList.get(position).getStatusInfoModels());
            mContext.startActivity(intent);
        });
    }

    public void setData(ArrayList<CompleteStatusInfo> arrayList) {
        mInfoArrayList = arrayList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return mInfoArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
    private CircularImageView mImageView;
    private TextView mTextViewName,mTextViewDate;

        public MyViewHolder(View itemView) {
            super(itemView);
           // mImageView = itemView.findViewById(R.id.txtView_status_name);
            mTextViewName = itemView.findViewById(R.id.txtView_status_name);
            mImageView = itemView.findViewById(R.id.img_other_status);
            mTextViewDate = itemView.findViewById(R.id.txtView_status_date);


        }
    }
}
