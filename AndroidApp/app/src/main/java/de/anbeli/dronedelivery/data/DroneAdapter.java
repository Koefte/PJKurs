package de.anbeli.dronedelivery.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.util.listeners.onRequestClickListener;

public class DroneAdapter extends RecyclerView.Adapter<DroneAdapter.DroneViewHolder> {

    private List<Drone> drone_list;
    private onRequestClickListener listener;
    Context c;

    public DroneAdapter(List<Drone> delivery_list, Context c, onRequestClickListener listener) {
        this.drone_list = delivery_list;
        this.listener = listener;
        this.c = c;
    }


    public class DroneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public DroneViewHolder(@NonNull View itemView) {
            super(itemView);

            drone_name = (TextView) itemView.findViewById(R.id.delivery_text);
            hardware_id = (TextView) itemView.findViewById(R.id.delivery_state_text);

            itemView.setOnClickListener(this);
        }

        public TextView drone_name;
        public TextView hardware_id;

        @Override
        public void onClick(View v) {
            listener.deliveryListItemClicked(v, this.getLayoutPosition());
        }
    }
    @Override
    public DroneAdapter.DroneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.delivery_item, parent, false);

        DroneAdapter.DroneViewHolder viewHolder = new DroneAdapter.DroneViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DroneAdapter.DroneViewHolder holder, int position) {
        Drone drone = drone_list.get(position);

        TextView drone_name = holder.drone_name;
        TextView hardware_id = holder.hardware_id;
        drone_name.setText(drone.get_name());
        hardware_id.setText(Long.toString(drone.get_hardware_id()));
    }

    @Override
    public int getItemCount() {
        return drone_list.size();
    }

    private String getDeliveryTextState(Delivery.delivery_state state) {
        switch(state) {
            case DELIVERY_IN_PROGRESS:
                return c.getString(R.string.text_deliver_state_in_progress);
            case DELIVERY_COMPLETE:
                return c.getString(R.string.text_deliver_state_complete);
            case TO_BE_DELIVERED:
                return c.getString(R.string.text_deliver_state_to_be_delivered);
            case TO_BE_CONFIRMED:
                return c.getString(R.string.text_deliver_state_to_be_confirmed);
        }
        return null;
    }
}