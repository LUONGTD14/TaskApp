package com.example.taskapp;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskapp.databinding.ActivityViewTaskBinding;
import com.example.taskapp.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ViewTaskActivity extends AppCompatActivity {
    private ActivityViewTaskBinding binding;
    private Map<String, String> categoryNameMap = new HashMap<>();
    private Map<String, String> memberNameMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.example.taskapp.databinding.ActivityViewTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String json = getIntent().getStringExtra("taskToView");
        if (json != null) {
            Task task = new Gson().fromJson(json, Task.class);
            loadCategoryAndMembers(task);
        }
    }

    private void loadCategoryAndMembers(Task task) {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        db.child("categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    categoryNameMap.put(snap.getKey(), snap.child("name").getValue(String.class));
                }

                db.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            memberNameMap.put(snap.getKey(), snap.child("name").getValue(String.class));
                        }
                        showTask(task);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showTask(Task task) {
        binding.txtName.setText(task.getName());
        binding.txtRequirement.setText(task.getRequirement().replace("\\n", "\n"));
        binding.txtCategory.setText(categoryNameMap.get(task.getCategoryId()));
        binding.txtMember1.setText(memberNameMap.get(task.getMember1Id()));
        binding.txtMember2.setText(memberNameMap.get(task.getMember2Id()));
        binding.txtManager.setText(memberNameMap.get(task.getManagerId()));
        binding.txtStartTime.setText(task.getStartTime());
        binding.txtEndTime.setText(task.getEndTime());
        binding.txtReminderTime.setText(task.getReminderTime());
        binding.txtDoneTime.setText(task.getDoneTime());
        binding.txtCreateTime.setText(task.getCreateTime());
        binding.txtStatus.setText(task.getStatus().name());
        binding.txtNumReminder.setText(String.valueOf(task.getNumOfReminder()));
        binding.txtExtendReason.setText(task.getExtendReason().replace("\\n", "\n"));
        Log.e("luongtd", task.getExtendReason().replace("\\n", "\n"));
    }
}

