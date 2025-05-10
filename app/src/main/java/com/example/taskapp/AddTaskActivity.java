package com.example.taskapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskapp.databinding.ActivityAddTaskBinding;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AddTaskActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;

    private EditText editTaskName, editRequirement, editStartTime, editEndTime, editReminderTime;
    private Spinner spinnerCategory, spinnerMember1, spinnerMember2, spinnerManager;
    private Button btnSaveTask;

    private Map<String, String> categoryMap = new HashMap<>();
    private Map<String, String> memberMap = new HashMap<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    private boolean isEditMode = false;
    private String taskIdToUpdate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.taskapp.databinding.ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        isEditMode = false;

        loadCategories();
        loadMembers();

        setDefaultTimes();

        Intent intent = getIntent();
        String json = intent.getStringExtra("taskToEdit");
        if (json != null) {
            isEditMode = true;
            Task task = new Gson().fromJson(json, Task.class);
            taskIdToUpdate = task.getId();
            loadTaskToForm(task);
        }


        binding.editStartTime.setOnClickListener(v -> showDateTimePicker(binding.editStartTime));
        binding.editEndTime.setOnClickListener(v -> showDateTimePicker(binding.editEndTime));
        binding.editReminderTime.setOnClickListener(v -> showDateTimePicker(binding.editReminderTime));

        binding.btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void loadTaskToForm(Task task) {
        binding.editTaskName.setText(task.getName());
        binding.editTaskName.setEnabled(false);

        binding.editRequirement.setText(task.getRequirement());

        binding.spinnerCategory.setEnabled(false);

        binding.editStartTime.setText(task.getStartTime());
        binding.editStartTime.setEnabled(false);

        binding.editEndTime.setText(task.getEndTime());
        binding.editEndTime.setEnabled(false);

        binding.editReminderTime.setText(task.getReminderTime());
        binding.editReminderTime.setEnabled(false);

        setSpinnerSelectionById(binding.spinnerMember1, task.getMember1Id());
        setSpinnerSelectionById(binding.spinnerMember2, task.getMember2Id());
        setSpinnerSelectionById(binding.spinnerManager, task.getManagerId());
    }

    private void loadCategories() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> names = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String id = snap.getKey();
                    String name = snap.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        names.add(name);
                        categoryMap.put(name, id);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTaskActivity.this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCategory.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadMembers() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> names = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String id = snap.getKey();
                    String name = snap.child("name").getValue(String.class);
                    if (id != null && name != null) {
                        names.add(name);
                        memberMap.put(name, id);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTaskActivity.this, android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerMember1.setAdapter(adapter);
                binding.spinnerMember2.setAdapter(adapter);
                binding.spinnerManager.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setSpinnerSelectionById(Spinner spinner, String id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String name = spinner.getItemAtPosition(i).toString();
            if (memberMap.containsKey(name) && memberMap.get(name).equals(id)) {
                spinner.setSelection(i);
                return;
            }
            if (categoryMap.containsKey(name) && categoryMap.get(name).equals(id)) {
                spinner.setSelection(i);
                return;
            }
        }
    }


    private void setDefaultTimes() {
        Calendar now = Calendar.getInstance();
        binding.editStartTime.setText(dateFormat.format(now.getTime()));

        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        binding.editEndTime.setText(dateFormat.format(end.getTime()));

        Calendar reminder = (Calendar) end.clone();
        reminder.add(Calendar.HOUR_OF_DAY, -4);
        binding.editReminderTime.setText(dateFormat.format(reminder.getTime()));
    }

    private void showDateTimePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            new TimePickerDialog(this, (v, hour, minute) -> {
                calendar.set(year, month, day, hour, minute);
                target.setText(dateFormat.format(calendar.getTime()));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveTask() {
        String name = binding.editTaskName.getText().toString().trim();
        String req = binding.editRequirement.getText().toString().trim();

        if (name.isEmpty() || req.isEmpty()) {
            Toast.makeText(this, "Please enter the information", Toast.LENGTH_SHORT).show();
            return;
        }
        req = req.replace("\n", " \\n ");
        String categoryId = categoryMap.get(binding.spinnerCategory.getSelectedItem().toString());
        String member1Id = memberMap.get(binding.spinnerMember1.getSelectedItem().toString());
        String managerId = memberMap.get(binding.spinnerManager.getSelectedItem().toString());
        String member2Id = binding.spinnerMember2.getSelectedItem() != null ?
                memberMap.get(binding.spinnerMember2.getSelectedItem().toString()) : null;

        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");

        if (isEditMode && taskIdToUpdate != null) {
            // Update Task
            String finalReq = req;
            taskRef.child(taskIdToUpdate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Task oldTask = snapshot.getValue(Task.class);
                    if (oldTask != null) {
                        oldTask.setRequirement(finalReq);
                        oldTask.setManagerId(managerId);
                        oldTask.setMember1Id(member1Id);
                        oldTask.setMember2Id(member2Id);

                        taskRef.child(taskIdToUpdate).setValue(oldTask)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(AddTaskActivity.this, "Update task successful", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        } else {
            // New Task
            String taskId = UUID.randomUUID().toString();
            String now = dateFormat.format(Calendar.getInstance().getTime());

            Task task = new Task(
                    taskId,
                    name,
                    req,
                    categoryId,
                    member1Id,
                    member2Id,
                    managerId,
                    binding.editStartTime.getText().toString(),
                    binding.editEndTime.getText().toString(),
                    "", // doneTime
                    now,
                    binding.editReminderTime.getText().toString(),
                    "", // extendReason
                    TaskStatus.CREATED,
                    10
            );

            taskRef.child(taskId).setValue(task)
                    .addOnSuccessListener(v -> {
                        Toast.makeText(this, "Add task successful", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }

}
