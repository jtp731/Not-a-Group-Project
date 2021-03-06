package com.example.roadsideassistance;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Spinner;

import java.util.Date;

@Entity(foreignKeys = {
        @ForeignKey(entity = RoadsideAssistant.class,
                    parentColumns = "username",
                    childColumns = "roadside_assistant_username",
                    onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Customer.class,
                    parentColumns = "username",
                    childColumns = "customer_username",
                    onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Car.class,
                    parentColumns = "plateNum",
                    childColumns = "car_plateNum",
                    onDelete = ForeignKey.CASCADE),
        },
        primaryKeys = {"roadside_assistant_username", "customer_username", "car_plateNum", "time"},
        indices = {@Index(value = {"customer_username", "car_plateNum", "time"}), @Index(value = {"roadside_assistant_username"}), @Index(value = "car_plateNum")}
)
public class Service implements Parcelable {
        //Filter Flags
        @Ignore
        public static final byte FLAT_TYRE = 0b00000001;
        @Ignore
        public static final byte FLAT_BATTERY = 0b00000010;
        @Ignore
        public static final byte MECHANICAL_BREAKDOWN = 0b0000100;
        @Ignore
        public static final byte KEYS_IN_CAR = 0b00001000;
        @Ignore
        public static final byte OUT_OF_FUEL = 0b00100000;
        @Ignore
        public static final byte CAR_STUCK = 0b01000000;
        @Ignore
        public static final byte OTHER = (byte) 0b10000000;

        //Service Status Constants
        @Ignore
        public static final int OPEN = 0;
        @Ignore
        public static final int ACCEPTED = 1;
        @Ignore
        public static final int FINISHED = 2;
        @Ignore
        public static final int PAYED_WITH_CARD = 3;
        @Ignore
        public static final int PAYED_WITH_SUB = 4;

        public float cost;
        @NonNull
        public Date time;
        public double latitude;
        public double longitude;
        public int status;
        public byte filter;
        public String description;

        @NonNull
        public String roadside_assistant_username;
        @NonNull
        public String customer_username;
        @NonNull
        public String car_plateNum;

        @Ignore
        public Service(RoadsideAssistant roadsideAssistant, Customer customer, Car car, float cost, Date time, double latitude, double longitude, int status, byte filter, String description) {
            this.roadside_assistant_username = roadsideAssistant.username;
            this.customer_username = customer.username;
            this.car_plateNum = car.plateNum;
            this.cost = cost;
            this.time = time;
            this.latitude = latitude;
            this.longitude = longitude;
            this.status = status;
            this.filter = filter;
            this.description = description;
        }

        public Service(String roadside_assistant_username, String customer_username, String car_plateNum, double latitude, double longitude, Date time, float cost, int status, byte filter, String description) {
            this.customer_username = customer_username;
            this.car_plateNum = car_plateNum;
            this.latitude = latitude;
            this.longitude = longitude;
            this.roadside_assistant_username = roadside_assistant_username;
            this.time = time;
            this.cost = cost;
            this.status = status;
            this.filter = filter;
            this.description = description;
        }

        @Ignore
        public Service(String customer_username, String car_plateNum, double latitude, double longitude, byte filter, String description) {
            this.customer_username = customer_username;
            this.car_plateNum = car_plateNum;
            this.latitude = latitude;
            this.longitude = longitude;
            this.filter = filter;
            this.description = description;
            time = new Date();
            cost = 0;
            roadside_assistant_username = "";
            status = 0;
        }

        @Ignore
        public Service(String customer_username, String car_plateNum, double latitude, double longitude, Date date, byte filter, String description) {
            this.customer_username = customer_username;
            this.car_plateNum = car_plateNum;
            this.latitude = latitude;
            this.longitude = longitude;
            this.filter = filter;
            this.description = description;
            this.time = date;
            cost = 0;
            roadside_assistant_username = "";
            status = OPEN;
        }

    @Override
    public String toString() {
            return ("User: " + customer_username + " Plate Number: " + car_plateNum);
    }

    @Ignore
    public String descriptionString() {
        String descriptionString = "";
        if(description.trim().length() > 0)
            descriptionString = "Description : " + this.description.trim();
        if(this.hasFlag(Service.OUT_OF_FUEL))
            descriptionString += "\nOut of fuel";
        if(this.hasFlag(Service.CAR_STUCK))
            descriptionString += "\nCar stuck";
        if(this.hasFlag(Service.KEYS_IN_CAR))
            descriptionString += "\nKeys locked in car";
        if(this.hasFlag(Service.FLAT_BATTERY))
            descriptionString += "\nFlat battery";
        if(this.hasFlag(Service.FLAT_TYRE))
            descriptionString += "\nFlat tyre";
        if(this.hasFlag(Service.MECHANICAL_BREAKDOWN))
            descriptionString += "\nMechanical breakdown";
        return descriptionString;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(cost);
        out.writeLong(time.getTime());
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeInt(status);
        out.writeString(customer_username);
        out.writeString(roadside_assistant_username);
        out.writeString(car_plateNum);
        out.writeByte(filter);
        out.writeString(description);
    }

    public static final Parcelable.Creator<Service> CREATOR = new Parcelable.Creator<Service>() {
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    private Service(Parcel in) {
        this.cost = in.readFloat();
        this.time = new Date(in.readLong());
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.status = in.readInt();
        this.customer_username = in.readString();
        this.roadside_assistant_username = in.readString();
        this.car_plateNum = in.readString();
        this.filter = in.readByte();
        this.description = in.readString();
    }

    boolean hasFlag(byte flag) {
        if((this.filter & flag) >= 1)
            return true;
        return false;
    }

    void setFlag(byte flag) {
        this.filter = (byte)(this.filter | flag);
    }

    public boolean equals(Service service) {
        if(
                this.roadside_assistant_username.equals(service.roadside_assistant_username)
                && this.customer_username.equals(service.customer_username)
                && this.car_plateNum.equals(service.car_plateNum)
                && this.time.equals(service.time)
        )
            return true;
        return false;
    }

    public float costToPay() {
        return (cost - 10);
    }
}
