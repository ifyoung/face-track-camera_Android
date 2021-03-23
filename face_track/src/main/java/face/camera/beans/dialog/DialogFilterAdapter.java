package face.camera.beans.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import face.camera.beans.R;
import face.camera.beans.base.utils.ConstantUtils;

import static face.camera.beans.base.utils.StaticFinalValues.IMG_FILTERS;


/**
 * Created by 钉某人
 * github: https://github.com/DingMouRen
 * email: naildingmouren@gmail.com
 */

public class DialogFilterAdapter extends RecyclerView.Adapter<DialogFilterAdapter.ViewHolder> {

    private OnItemClickListener mOnItemClickListener;

    public DialogFilterAdapter(Context context){
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_filter,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.img.setImageResource(IMG_FILTERS[position]);
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mOnItemClickListener) mOnItemClickListener.onItemClickListener(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return IMG_FILTERS.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        public ViewHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener{
        void onItemClickListener(int position);
    }
}
