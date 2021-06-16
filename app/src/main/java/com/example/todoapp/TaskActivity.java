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
import android.widget.TextView;
import android.widget.Toast;

import com.example.todoapp.Objects.Project;
import com.example.todoapp.Objects.ProjectTask;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TaskActivity extends AppCompatActivity {

    private static final String DESCRIPTION_KEY = "description";
    private static final String TO_DATE_KEY = "toDate";
    private static final String COMPLETED_KEY = "completed";
    private static final String PROJECT_KEY = "project";
    private static final String TASKS_COLLECTION_KEY = "tasks";
    private static final String PROJECT_EXTRA_KEY = "projectExtra";
    private static final String TASK_EXTRA_KEY = "taskExtra";
    private static final String TAG = "taskActivityLogTag";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    ProjectTask projectTaskExtra;
    Project projectExtra;
    Boolean taskCompleted = false;

    TextView projectNameTextView;
    EditText toDateEditText;
    Button saveBtn;
    Button deleteTaskBtn;
    Button taskStatusBtn;
    EditText descriptionEditText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        projectNameTextView = findViewById(R.id.projectName1);
        descriptionEditText = findViewById(R.id.taskDescription);
        toDateEditText = findViewById(R.id.taskToDate);
        saveBtn = findViewById(R.id.saveTaskBtn);
        deleteTaskBtn = findViewById(R.id.deleteTaskBtn);
        taskStatusBtn = findViewById(R.id.taskStatusBtn);

        toDateEditText.setInputType(InputType.TYPE_NULL);

        if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
            projectExtra = getIntent().getParcelableExtra(PROJECT_EXTRA_KEY);
            if (getIntent().hasExtra(TASK_EXTRA_KEY)) {
                projectTaskExtra = getIntent().getParcelableExtra(TASK_EXTRA_KEY);
                projectNameTextView.setText(projectExtra.getDescription());
                descriptionEditText.setText(projectTaskExtra.getDescription());
                toDateEditText.setText(projectTaskExtra.getToDate());
                taskCompleted = projectTaskExtra.isCompleted();
                setStatusBtn();
            } else {
                // disabling delete and status buttons when creating a new task
                deleteTaskBtn.setVisibility(View.INVISIBLE);
                deleteTaskBtn.setEnabled(false);
                taskStatusBtn.setVisibility(View.INVISIBLE);
                taskStatusBtn.setEnabled(false);
            }
        }


        toDateEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                toDateEditText.requestFocus();
                final Calendar cldr = Calendar.getInstance();
                int calendarDay = cldr.get(Calendar.DAY_OF_MONTH);
                int calendarMonth = cldr.get(Calendar.MONTH);
                int calendarYear = cldr.get(Calendar.YEAR);

                DatePickerDialog picker = new DatePickerDialog(TaskActivity.this,
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

        taskStatusBtn.setOnClickListener(v -> {
            taskCompleted = !taskCompleted;
            setStatusBtn();
        });

        saveBtn.setOnClickListener(v -> {

            String description = descriptionEditText.getText().toString();
            String toDate = toDateEditText.getText().toString();

            if (toDate.length() > 0 && description.length() > 0) {
                Map<String, Object> projectTask = new HashMap<>();
                projectTask.put(DESCRIPTION_KEY, description);
                projectTask.put(TO_DATE_KEY, toDate);
                projectTask.put(PROJECT_KEY, projectExtra.getId());
                projectTask.put(COMPLETED_KEY, taskCompleted);

                if (getIntent().hasExtra(PROJECT_EXTRA_KEY) && getIntent().hasExtra(TASK_EXTRA_KEY)) {
                    db.collection(TASKS_COLLECTION_KEY).document(projectTaskExtra.getId())
                            .update(projectTask);
                }
                else if (getIntent().hasExtra(PROJECT_EXTRA_KEY)) {
                    db.collection(TASKS_COLLECTION_KEY)
                            .add(projectTask)
                            .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId()))
                            .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                }

                Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
                intent.putExtra(PROJECT_EXTRA_KEY, projectExtra);
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

        deleteTaskBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(TaskActivity.this);

            builder.setMessage(R.string.delete_alert_message)
                    .setTitle(R.string.delete_task_alert_title);

            builder.setPositiveButton(R.string.alert_positive_btn, (dialog, id) -> {
                if (getIntent().hasExtra(TASK_EXTRA_KEY)) {
                    db.collection(TASKS_COLLECTION_KEY).document(projectTaskExtra.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                Intent intent = new Intent(getApplicationContext(), TasksActivity.class);
                                intent.putExtra(PROJECT_EXTRA_KEY, projectExtra);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Error deleting project", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "Error deleting document", e);
                            });
                }
            });
            builder.setNegativeButton(R.string.alert_negative_btn, (dialog, id) -> {
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        });

    }

    private void setStatusBtn() {
        if (taskCompleted) {
            taskStatusBtn.setBackgroundColor(getResources().getColor(R.color.dgreen));
            taskStatusBtn.setText(R.string.status_completed_btn);
        } else {
            taskStatusBtn.setBackgroundColor(Color.RED);
            taskStatusBtn.setText(R.string.status_uncompleted_btn);
        }
    }
}