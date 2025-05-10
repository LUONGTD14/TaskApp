package com.example.taskapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskapp.R;
import com.example.taskapp.adapter.DoneOverdueTaskAdapter;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoneOverdueTaskFragment extends Fragment {

    private RecyclerView recyclerView;
    private DoneOverdueTaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private Map<String, Member> memberMap = new HashMap<>();

    private DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
    private DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("members");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_done_overdue_task, container, false);

        recyclerView = view.findViewById(R.id.recyclerDoneOverdue);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DoneOverdueTaskAdapter(taskList, memberMap, getContext());
        recyclerView.setAdapter(adapter);

        loadMembersAndTasks();

        return view;
    }

    private void loadMembersAndTasks() {
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberMap.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Member member = child.getValue(Member.class);
                    memberMap.put(member.getId(), member);
                }

                loadTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void loadTasks() {
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.OVERDUE
                            || task.getStatus() == TaskStatus.IGNORE) {
                        taskList.add(task);
                    }
                }

                Collections.sort(taskList, (a, b) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                        Date dateA = sdf.parse(a.getCreateTime());
                        Date dateB = sdf.parse(b.getCreateTime());
                        return dateB.compareTo(dateA);
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

