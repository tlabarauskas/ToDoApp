package com.example.todoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.todoapp.Objects.Project;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProjectActivity extends AppCompatActivity {

    private static final String DESCRIPTION_KEY = "description";
    private static final String TO_DATE_KEY = "toDate";
    private static final String COMPLETED_KEY = "completed";
    private static final String PROJECTS_COLLECTION_KEY = "projects";
    private static final String TASKS_COLLECTION_KEY = "tasks";
    private static final String PROJECT_EXTRA_KEY = "projectExtra";
    private static final String TAG = "projectActivityLogTag";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Boolean projectCompleted = false;

    EditText toDateEditText;
    Button saveBtn;
    Button deleteProjectBtn;
    EditText descriptionEditText;
    Button projectStatusBtn;
    Project projectExtra;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        descriptionEditText = findViewById(R.id.projectDescription);
        toDateEditText = findViewById(R.id.projectToDate);
        saveBtn = findViewById(R.id.saveProjectBtn);
        deleteProjectBtn = findViewById(R.id.deleteProjectBtn);
        projectStatusBtn = findViewById(R.id.projectStatusBtn);

        toDateEditText.setInputType(InputType.TYPE_NULL);

        if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
            projectExtra = getIntent().getParcelableExtra(PROJECT_EXTRA_KEY);
            descriptionEditText.setText(projectExtra.getDescription());
            toDateEditText.setText(projectExtra.getToDate());
            projectCompleted = projectExtra.isCompleted();
            setStatusBtn();
        } else {
            // disabling delete and status buttons when creating a new project
            deleteProjectBtn.setVisibility(View.INVISIBLE);
            deleteProjectBtn.setEnabled(false);
            projectStatusBtn.setVisibility(View.INVISIBLE);
            projectStatusBtn.setEnabled(false);
        }

        toDateEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                toDateEditText.requestFocus();
                final Calendar cldr = Calendar.getInstance();
                int calendarDay = cldr.get(Calendar.DAY_OF_MONTH);
                int calendarMonth = cldr.get(Calendar.MONTH);
                int calendarYear = cldr.get(Calendar.YEAR);

                DatePickerDialog picker = new DatePickerDialog(ProjectActivity.this,
                        (view, year, monthOfYear, dayOfMonth) -> {

                            String month = (monthOfYear + 1) > 9 ? (monthOfYear + 1) + "" : "0" + (monthOfYear + 1);
                            String day = dayOfMonth > 9 ? dayOfMonth + "" : "0" + dayOfMonth;

                            String date = year + "-" + month + "-" + day;
                            toDateEditText.setText(date);
                            toDateEditText.setError(null);
                        }, calendarYear, calendarMonth, calendarDay);
                picker.show();
                return true;
            }
            return false;
        });

        projectStatusBtn.setOnClickListener(v -> {
            projectCompleted = !projectCompleted;
            setStatusBtn();
        });

        saveBtn.setOnClickListener(v -> {

            String description = descriptionEditText.getText().toString();
            String toDate = toDateEditText.getText().toString();

            if (toDate.length() > 0 && description.length() > 0) {
                Map<String, Object> project = new HashMap<>();
                project.put(DESCRIPTION_KEY, description);
                project.put(TO_DATE_KEY, toDate);
                project.put(COMPLETED_KEY, projectCompleted);

                //updating existing project
                if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
                    db.collection(PROJECTS_COLLECTION_KEY).document(projectExtra.getId())
                            .update(project);
                } //creating new project
                else {
                    db.collection(PROJECTS_COLLECTION_KEY)
                            .add(project)
                            .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                }

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                if (toDate.length() == 0) {
                    toDateEditText.setError("Field is mandatory");
                }
                if (description.length() == 0) {
                    descriptionEditText.setError("Field is mandatory");
                }
            }

        });

        deleteProjectBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProjectActivity.this);

            builder.setMessage(R.string.delete_alert_message)
                    .setTitle(R.string.delete_project_alert_title);

            builder.setPositiveButton(R.string.alert_positive_btn, (dialog, id) -> {
                if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
                    db.collection(PROJECTS_COLLECTION_KEY).document(projectExtra.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                deleteProjectTasks();
                                Log.d(TAG, "Project document successfully deleted!");
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> Log.w(TAG, "Error deleting project document", e));
                }
            });
            builder.setNegativeButton(R.string.alert_negative_btn, (dialog, id) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private void setStatusBtn() {
        if (projectCompleted) {
            projectStatusBtn.setBackgroundColor(getResources().getColor(R.color.dgreen));
            projectStatusBtn.setText(R.string.status_completed_btn);
        } else {
            projectStatusBtn.setBackgroundColor(Color.RED);
            projectStatusBtn.setText(R.string.status_uncompleted_btn);
        }
    }

    private void deleteProjectTasks() {
        db.collection(PROJECTS_COLLECTION_KEY).document(projectExtra.getId()).collection(TASKS_COLLECTION_KEY)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            db.collection(PROJECTS_COLLECTION_KEY).document(projectExtra.getId()).collection(TASKS_COLLECTION_KEY).document(document.getId()).delete();

                            Log.d(TAG, "Document from tasks collection successfully deleted!");
                        }
                    } else {
                        Log.w(TAG, "Error deleting task document.", task.getException());
                    }
                });
    }
}