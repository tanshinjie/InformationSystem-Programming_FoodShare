package utilities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshare.R;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import datatypes.OrderItem;
import datatypes.Request;
import managers.RequestManager;
import managers.RestaurantManager;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private static final String TAG = "OrderAdapter";
    private Context mContext;
    private List<OrderItem> orders;

    public OrderAdapter(Context mContext, List<OrderItem> orders){
        this.mContext = mContext;
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.order_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.ViewHolder holder, int position) {
        OrderItem order = orders.get(position);
        Request request = RequestManager.getInstance().getLastRetrievedRequest();
        double perPersonFee = request.getDeliveryFee()/orders.size();
        BigDecimal feeBd = BigDecimal.valueOf(perPersonFee);
        feeBd.setScale(2, RoundingMode.CEILING);
        BigDecimal totalBd = BigDecimal.valueOf(order.getTotalPrice() + feeBd.doubleValue());
        totalBd.setScale(2, RoundingMode.HALF_UP);


        // On update, remove all the dynamically added views to prevent duplication
        holder.removeExtraTextViews();

        holder.username.setText(order.getUsername());
        holder.foodCost.setText(String.format("%.2f", order.getTotalPrice())); // Format price display
        holder.deliveryFee.setText(String.format("%.2f", feeBd));
        holder.totalPrice.setText(String.format("%.2f", totalBd));

        holder.innerLayout.removeAllViews();

        int prevTextViewId = 0;
        // Dynamically add new text views based on the number of orders per person
        for (String foodname : order.getOrders()) {
            TextView nameView = new TextView(mContext);
            TextView priceView = new TextView(mContext);

            int curTextViewId = prevTextViewId + 1; // 1
            RelativeLayout.LayoutParams params1 =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams params2 =
                    new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

            params2.addRule(RelativeLayout.BELOW, curTextViewId);
            params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params2.setMargins(0, 0, 50, 0);

            nameView.setId(prevTextViewId);
            nameView.setText(foodname);
            params1.addRule(RelativeLayout.BELOW, curTextViewId);

            String restaurantName = request.getRestaurant();
            Double price = RestaurantManager.getInstance().getRestaurants().get(restaurantName).get(foodname);
            priceView.setText(String.format("%.2f", price));

            prevTextViewId = curTextViewId;

            holder.innerLayout.addView(nameView, params1);
            holder.innerLayout.addView(priceView, params2);
        }
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView username, totalPrice, deliveryFee, foodCost;
        RelativeLayout innerLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            totalPrice = itemView.findViewById(R.id.pricePerUser);
            innerLayout = itemView.findViewById(R.id.ordersSummary);
            deliveryFee = itemView.findViewById(R.id.deliveryFee);
            foodCost = itemView.findViewById(R.id.foodCost);
        }

        // Remove views other than the username (index 0)
        public void removeExtraTextViews(){
            int size = ((ViewGroup)itemView).getChildCount();
            ((ViewGroup)itemView).removeViews(1, size - 1);
        }

    }
}
