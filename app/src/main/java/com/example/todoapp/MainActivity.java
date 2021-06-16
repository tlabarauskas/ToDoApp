package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import com.example.todoapp.Adapters.ProjectArrayAdapter;
import com.example.todoapp.Objects.Project;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String DESCRIPTION_KEY = "description";
    private static final String TO_DATE_KEY = "toDate";
    private static final String COMPLETED_KEY = "completed";
    private static final String PROJECTS_COLLECTION_KEY = "projects";
    private static final String PROJECT_EXTRA_KEY = "projectExtra";
    private static final String TAG = "mainActivityLogTag";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<Project> projects = new ArrayList<>();

    ListView projectsListView;
    Button addProjectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        projectsListView = findViewById(R.id.projectsList);
        addProjectBtn = findViewById(R.id.addBtn);

        setProjectsListView();

        projectsListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
            intent.putExtra(PROJECT_EXTRA_KEY, projects.get(position));
            startActivity(intent);
        });

        projectsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
            intent.putExtra(PROJECT_EXTRA_KEY, projects.get(position));
            startActivity(intent);
            return true;
        });


        addProjectBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ProjectActivity.class);
            startActivity(intent);
        });
    }

    private void setProjectsListView() {

        db.collection(PROJECTS_COLLECTION_KEY)
                .orderBy(COMPLETED_KEY)
                .orderBy(TO_DATE_KEY)
                .orderBy(DESCRIPTION_KEY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Project project = new Project(document.getId(), Objects.requireNonNull(document.getData().get(DESCRIPTION_KEY)).toString(), Objects.requireNonNull(document.getData().get(TO_DATE_KEY)).toString(), (boolean) Objects.requireNonNull(document.getData().get(COMPLETED_KEY)));
                            projects.add(project);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }

                        ProjectArrayAdapter arrayAdapter = new ProjectArrayAdapter(getApplicationContext(), R.layout.adapter_view_layout, projects);
                        projectsListView.setAdapter(arrayAdapter);

                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });

    }
}