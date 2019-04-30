package com.example.roadsideassistance;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

@Entity(inheritSuperIndices = true)
public class RoadsideAssistant extends Person {
    boolean canTow;
    float rating;

    @Ignore
    public List<Review> reviews;

    @Ignore
    public List<Service> services;

    public RoadsideAssistant(String username, String password, String phonenumber, String email, String firstName, String lastName, boolean canTow, float rating) {
        super(username, password, phonenumber, email, firstName, lastName);
        this.canTow = canTow;
        this.rating = rating;
    }

    @Ignore
    public RoadsideAssistant(Person person, boolean canTow) {
        super(person);
        this.canTow = canTow;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
    }

    public static final Parcelable.Creator<RoadsideAssistant> CREATOR = new Parcelable.Creator<RoadsideAssistant>() {
        public RoadsideAssistant createFromParcel(Parcel in) {
            return new RoadsideAssistant(in);
        }

        public RoadsideAssistant[] newArray(int size) {
            return new RoadsideAssistant[size];
        }
    };

    private RoadsideAssistant(Parcel in) {
        super(in);
    }
}
