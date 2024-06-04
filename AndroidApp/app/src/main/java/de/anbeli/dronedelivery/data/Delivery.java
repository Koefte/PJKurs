package de.anbeli.dronedelivery.data;

import java.util.ArrayList;

public class Delivery {

    public enum delivery_state {
        TO_BE_CONFIRMED,
        TO_BE_DELIVERED,
        DELIVERY_IN_PROGRESS,
        DELIVERY_COMPLETE
    }
    private delivery_state state;
    private String receiver_email;
    private String geo_string;

    public Delivery(String receiver_email, delivery_state state) {
        this.receiver_email = receiver_email;
        this.state = state;
    }

    public Delivery(String receiver_email, delivery_state state, String geo_string) {
        this.receiver_email = receiver_email;
        this.state = state;
        this.geo_string = geo_string;
    }

    public delivery_state get_state() { return state; }

    public String get_receiver() { return receiver_email; }

    public String get_geo_string() { return geo_string; }

    public String toString() {
        return "[" + receiver_email + "]";
    }
}
