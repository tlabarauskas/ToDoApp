package com.example.todoapp.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class Project implements Parcelable {
    private final String id;
    private String description;
    private String toDate;
    private boolean completed;

    public Project(String id, String description, String toDate, boolean completed) {
        this.id = id;
        this.description = description;
        this.toDate = toDate;
        this.completed = completed;
    }

    protected Project(Parcel in) {
        id = in.readString();
        description = in.readString();
        toDate = in.readString();
        completed = in.readByte() != 0;
    }

    public static final Creator<Project> CREATOR = new Creator<Project>() {
        @Override
        public Project createFromParcel(Parcel in) {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size) {
            return new Project[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(toDate);
        dest.writeByte((byte) (completed ? 1 : 0));
    }
}
