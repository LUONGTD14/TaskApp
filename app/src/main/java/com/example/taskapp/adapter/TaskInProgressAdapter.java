package com.example.taskapp.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskapp.R;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TaskInProgressAdapter extends RecyclerView.Adapter<TaskInProgressAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Map<String, Member> memberMap;

    public TaskInProgressAdapter(List<Task> taskList, Map<String, Member> memberMap) {
        this.taskList = taskList;
        this.memberMap = memberMap;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textRemaining, textTimeRange, textName, textManagerMembers;
        LinearLayout layoutTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textRemaining = itemView.findViewById(R.id.text_remaining_time);
            textTimeRange = itemView.findViewById(R.id.text_time_range);
            textName = itemView.findViewById(R.id.text_task_name);
            textManagerMembers = itemView.findViewById(R.id.text_manager_members);
            layoutTask = itemView.findViewById(R.id.layout_task);
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_in_progress, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.textName.setText(task.getName());
        holder.textTimeRange.setText(task.getStartTime() + " - " + task.getEndTime());

        Member manager = memberMap.get(task.getManagerId());
        Member member1 = memberMap.get(task.getMember1Id());

        String managerName = (manager != null) ? manager.getName() : "N/A";
        String member1Name = (member1 != null) ? member1.getName() : "N/A";
        holder.textManagerMembers.setText("M: " + managerName + " - I: " + member1Name);

        // remain time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        try {
            Date now = new Date();
            Date end = sdf.parse(task.getEndTime());
            long diffMs = end.getTime() - now.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diffMs);
            long hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24;
            holder.textRemaining.setText("Remind: " + days + " days " + hours + " h");


            // background
            int bgColor = Color.GRAY;
            if (days <= 2) bgColor = Color.parseColor("#ea8b8b");//#ea8b8b
            else if (days <= 4) bgColor = Color.parseColor("#fdf5d4");
            else if (days <= 7) bgColor = Color.parseColor("#d4f1fd");
            else if (days > 7) bgColor = Color.parseColor("#F8F5F5");

            holder.layoutTask.setBackgroundColor(bgColor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: Gắn sự kiện 3 nút
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
}
