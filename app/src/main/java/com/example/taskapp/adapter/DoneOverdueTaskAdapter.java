package com.example.taskapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskapp.R;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;

import java.util.List;
import java.util.Map;

public class DoneOverdueTaskAdapter extends RecyclerView.Adapter<DoneOverdueTaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Map<String, Member> memberMap;

    public DoneOverdueTaskAdapter(List<Task> taskList, Map<String, Member> memberMap) {
        this.taskList = taskList;
        this.memberMap = memberMap;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_done_overdue, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textStatus.setText("Status: " + task.getStatus().name());
        holder.textStartEndTime.setText("Start time: " + task.getStartTime() + " \n Done time: " + task.getDoneTime());
        holder.textTaskName.setText(task.getName());

        String managerName = memberMap.containsKey(task.getManagerId()) ? memberMap.get(task.getManagerId()).getName() : "unknown";
        String member1Name = memberMap.containsKey(task.getMember1Id()) ? memberMap.get(task.getMember1Id()).getName() : "unknown";

        holder.textManagerMember.setText("M: " + managerName + " - I: " + member1Name);

        // background
        if (task.getStatus() == TaskStatus.DONE) {
            holder.itemView.setBackgroundColor(Color.parseColor("#F8F5F5"));
        } else if (task.getStatus() == TaskStatus.OVERDUE) {
            holder.itemView.setBackgroundColor(Color.parseColor("#ea8b8b"));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textStatus, textStartEndTime, textTaskName, textManagerMember;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textStatus = itemView.findViewById(R.id.textStatus);
            textStartEndTime = itemView.findViewById(R.id.textStartEndTime);
            textTaskName = itemView.findViewById(R.id.textTaskName);
            textManagerMember = itemView.findViewById(R.id.textManagerMember);
        }
    }
}
