package utilities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshare.MainActivity;
import com.example.foodshare.R;
import com.example.foodshare.RequestActivity;

import java.util.List;

import datatypes.Request;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";
    private List<Request> requests;
    private Context mContext;

    public RecyclerViewAdapter( Context mContext, List<Request> requests) {
        this.requests = requests;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Request current = requests.get(position);
        holder.restName.setText(current.getRestaurant());
        holder.location.setText(current.getLocation().getName());
        holder.participants.setText(Integer.toString(current.getOrders().size()));
        holder.orderby.setText(current.getOrderByTime());
        holder.remarks.setText(current.getRemarks());
        holder.owner.setText(current.getOwnerName());

        if (current.getRestaurant().equals("KFC")) {
            holder.logo.setImageResource(R.drawable.kfc_logo);
        } else if (current.getRestaurant().equals("McDonald's")){
            holder.logo.setImageResource(R.drawable.macs_logo);
        } else {
            holder.logo.setBackgroundColor(Color.rgb(192,192,192));
        }

        holder.setRequest(current);
    }



    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView restName, location, owner, orderby, remarks, participants;
        private Request request;
        ImageView logo;

        public void setRequest(Request request) {
            this.request = request;
        }

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restName = itemView.findViewById(R.id.home_restaurant);
            location = itemView.findViewById(R.id.home_location);
            owner = itemView.findViewById(R.id.home_owner);
            remarks = itemView.findViewById(R.id.home_remarks);
            orderby = itemView.findViewById(R.id.home_orderby);
            participants = itemView.findViewById(R.id.home_participants);
            logo = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, RequestActivity.class);
                    intent.putExtra(MainActivity.SELECTED_REQUEST_ID, request.getRequestID());
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
