package com.example.taskapp.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.taskapp.R;
import com.example.taskapp.adapter.TaskInProgressAdapter;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InProgressTaskFragment extends Fragment {

    private RecyclerView recyclerView;
    private TaskInProgressAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private Map<String, Member> memberMap = new HashMap<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    public InProgressTaskFragment() {
        super(R.layout.fragment_in_progress_task);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_in_progress);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskInProgressAdapter(taskList, memberMap, getContext());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::reloadData);

        reloadData();

        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener());
    }

    private void reloadData() {
        swipeRefreshLayout.setRefreshing(true);

        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("members");
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                memberMap.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Member m = ds.getValue(Member.class);
                    memberMap.put(m.getId(), m);
                }

                loadTasks();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadTasks() {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Task task = ds.getValue(Task.class);
                    if (task.getStatus() == TaskStatus.CREATED || task.getStatus() == TaskStatus.PENDING) {
                        taskList.add(task);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                taskList.sort((t1, t2) -> {
                    try {
                        return sdf.parse(t2.getEndTime()).compareTo(sdf.parse(t1.getEndTime()));
                    } catch (Exception e) {
                        return 0;
                    }
                });

                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
