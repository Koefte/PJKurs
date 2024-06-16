package de.anbeli.dronedelivery.data;

public class Drone {

    String name;
    long hardware_id;

    public Drone(String name, long hardware_id) {
        this.name = name;
        this.hardware_id = hardware_id;
    }

    public String get_name() {
        return name;
    }

    public long get_hardware_id() {
        return hardware_id;
    }
 }
