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

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.DeliveryViewHolder> {

    private List<Delivery> delivery_list;
    private onRequestClickListener listener;
    Context c;

    public DeliveryAdapter(List<Delivery> delivery_list, Context c, onRequestClickListener listener) {
        this.delivery_list = delivery_list;
        this.listener = listener;
        this.c = c;
    }


    public class DeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public DeliveryViewHolder(@NonNull View itemView) {
            super(itemView);

            delivery_text = (TextView) itemView.findViewById(R.id.delivery_text);
            delivery_state_text = (TextView) itemView.findViewById(R.id.delivery_state_text);

            itemView.setOnClickListener(this);
        }

        public TextView delivery_text;
        public TextView delivery_state_text;

        @Override
        public void onClick(View v) {
            listener.deliveryListItemClicked(v, this.getLayoutPosition());
        }
    }
    @Override
    public DeliveryAdapter.DeliveryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.delivery_item, parent, false);

        DeliveryViewHolder viewHolder = new DeliveryViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryAdapter.DeliveryViewHolder holder, int position) {
        Delivery delivery = delivery_list.get(position);

        TextView delivery_text = holder.delivery_text;
        TextView delivery_state_text = holder.delivery_state_text;
        delivery_text.setText(delivery.get_receiver());
        delivery_state_text.setText(getDeliveryTextState(delivery.get_state()));
    }

    @Override
    public int getItemCount() {
        return delivery_list.size();
    }

    private String getDeliveryTextState(Delivery.delivery_state state) {
        //Method to match enum values to Strings

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
