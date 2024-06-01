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

    public Delivery(String receiver_email, delivery_state state) {
        this.receiver_email = receiver_email;
        this.state = state;
    }

    public delivery_state get_state() { return state; }

    public String get_receiver() { return receiver_email; }

    public static ArrayList<Delivery> createContactsList(int amount) {
        ArrayList<Delivery> contacts = new ArrayList<Delivery>();

        for (int i = 1; i <= amount; i++) {
            contacts.add(new Delivery("Test " + i, delivery_state.TO_BE_CONFIRMED));
        }

        return contacts;
    }
}
