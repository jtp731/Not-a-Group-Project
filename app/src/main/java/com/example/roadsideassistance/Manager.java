package com.example.roadsideassistance;

import android.arch.persistence.room.Entity;

@Entity
public class Manager extends Person{
    public int accessLevel;

    public Manager(String username, String email, String firstName, String lastName, int accessLevel) {
        super(username, email, firstName, lastName);
        this.accessLevel = accessLevel;
    }
}
