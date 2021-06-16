package com.example.todoapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todoapp.Objects.Project;
import com.example.todoapp.R;

import java.util.ArrayList;

public class ProjectArrayAdapter extends ArrayAdapter<Project> {


    private final Context mContext;
    private final int mResource;

    static class ViewHolder {
        TextView descriptionTextView;
        TextView toDateTextView;
        ImageView completedImageView;
    }

    public ProjectArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Project> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String description = getItem(position).getDescription();
        String toDate = getItem(position).getToDate();
        boolean completed = getItem(position).isCompleted();

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(mResource, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.descriptionTextView = convertView.findViewById(R.id.itemDescriptionTextView);
            viewHolder.toDateTextView = convertView.findViewById(R.id.itemToDateTextView);
            viewHolder.completedImageView = convertView.findViewById(R.id.completedImageView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.descriptionTextView.setText(description);
        viewHolder.toDateTextView.setText(toDate);
        if (completed) {
            viewHolder.completedImageView.setImageResource(R.drawable.check);
        }
        else {
            viewHolder.completedImageView.setImageResource(0);
        }

        return convertView;
    }
}
