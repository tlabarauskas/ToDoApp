package com.example.todoapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todoapp.Adapters.TaskArrayAdapter;
import com.example.todoapp.Objects.Project;
import com.example.todoapp.Objects.ProjectTask;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class TasksActivity extends AppCompatActivity {

    private static final String DESCRIPTION_KEY = "description";
    private static final String TO_DATE_KEY = "toDate";
    private static final String COMPLETED_KEY = "completed";
    private static final String PROJECTS_COLLECTION_KEY = "projects";
    private static final String TASKS_COLLECTION_KEY = "tasks";
    private static final String PROJECT_EXTRA_KEY = "projectExtra";
    private static final String TASK_EXTRA_KEY = "taskExtra";
    private static final String TAG = "tasksActivityLogTag";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ArrayList<ProjectTask> projectTasks = new ArrayList<>();

    ListView tasksListView;
    TextView projectNameTextView;
    Project projectExtra;
    Button addTaskBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        tasksListView = findViewById(R.id.tasksList);
        projectNameTextView = findViewById(R.id.projectName);
        addTaskBtn = findViewById(R.id.addTaskBtn);

        if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
            projectExtra = getIntent().getParcelableExtra(PROJECT_EXTRA_KEY);
            projectNameTextView.setText(projectExtra.getDescription());
            setTasksListView();
        }

        tasksListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
            intent.putExtra(TASK_EXTRA_KEY, projectTasks.get(position));
            intent.putExtra(PROJECT_EXTRA_KEY, projectExtra);
            startActivity(intent);
        });

        addTaskBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
            intent.putExtra(PROJECT_EXTRA_KEY, projectExtra);
            startActivity(intent);
        });

    }

    private void setTasksListView() {

        db.collection(PROJECTS_COLLECTION_KEY).document(projectExtra.getId()).collection(TASKS_COLLECTION_KEY)
                .orderBy(COMPLETED_KEY)
                .orderBy(TO_DATE_KEY)
                .orderBy(DESCRIPTION_KEY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            ProjectTask projectTask = new ProjectTask(document.getId(), Objects.requireNonNull(document.getData().get(DESCRIPTION_KEY)).toString(), Objects.requireNonNull(document.getData().get(TO_DATE_KEY)).toString(), (boolean) Objects.requireNonNull(document.getData().get(COMPLETED_KEY)));
                            projectTasks.add(projectTask);
                            TaskArrayAdapter arrayAdapter = new TaskArrayAdapter(getApplicationContext(), R.layout.adapter_view_layout, projectTasks);
                            tasksListView.setAdapter(arrayAdapter);
                            Log.d(TAG, document.getId() + " => " + document.getData());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}
