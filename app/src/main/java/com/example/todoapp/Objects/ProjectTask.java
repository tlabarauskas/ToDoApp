package com.example.todoapp.Objects;

import android.os.Parcel;
import android.os.Parcelable;

public class ProjectTask implements Parcelable {
    private final String id;
    private String description;
    private String toDate;
    private boolean completed;

    public ProjectTask(String id, String description, String toDate, boolean completed) {
        this.id = id;
        this.description = description;
        this.toDate = toDate;
        this.completed = completed;
    }

    protected ProjectTask(Parcel in) {
        id = in.readString();
        description = in.readString();
        toDate = in.readString();
        completed = in.readByte() != 0;
    }

    public static final Creator<ProjectTask> CREATOR = new Creator<ProjectTask>() {
        @Override
        public ProjectTask createFromParcel(Parcel in) {
            return new ProjectTask(in);
        }

        @Override
        public ProjectTask[] newArray(int size) {
            return new ProjectTask[size];
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
