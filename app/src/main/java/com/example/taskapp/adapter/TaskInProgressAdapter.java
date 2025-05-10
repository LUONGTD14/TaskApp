package com.example.taskapp.adapter;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskapp.AddTaskActivity;
import com.example.taskapp.R;
import com.example.taskapp.ViewTaskActivity;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

public class TaskInProgressAdapter extends RecyclerView.Adapter<TaskInProgressAdapter.TaskViewHolder> {
    private Context context;

    private List<Task> taskList;
    private Map<String, Member> memberMap;

    public TaskInProgressAdapter(List<Task> taskList, Map<String, Member> memberMap, Context context) {
        this.taskList = taskList;
        this.memberMap = memberMap;
        this.context = context;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView textRemaining, textTimeRange, textName, textManagerMembers;
        Button btn_change_status, btn_edit, btn_extend;
        LinearLayout layoutTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textRemaining = itemView.findViewById(R.id.text_remaining_time);
            textTimeRange = itemView.findViewById(R.id.text_time_range);
            textName = itemView.findViewById(R.id.text_task_name);
            btn_change_status = itemView.findViewById(R.id.btn_change_status);
            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_extend = itemView.findViewById(R.id.btn_extend);
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
        holder.btn_edit.setOnClickListener(v -> {
            Intent intent = new Intent(context, AddTaskActivity.class);
            intent.putExtra("taskToEdit", new Gson().toJson(task)); // hoặc Parcelable nếu bạn có
            context.startActivity(intent);
        });
        holder.btn_extend.setOnClickListener(v -> {
            showExtendDialog(context, task);
        });
        holder.btn_change_status.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Select New Status");

            String[] statusList = {"CREATED", "PENDING", "DONE", "OVERDUE", "IGNORE"};
            builder.setItems(statusList, (dialog, which) -> {
                String selected = statusList[which];
                task.setStatus(TaskStatus.valueOf(selected));
                if (selected.equals("DONE") || selected.equals("IGNORE")) {
                    String doneTime = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                            .format(Calendar.getInstance().getTime());
                    task.setDoneTime(doneTime);
                }

                FirebaseDatabase.getInstance().getReference("tasks")
                        .child(task.getId())
                        .setValue(task)
                        .addOnSuccessListener(unused ->
                                Toast.makeText(context, "Status updated to " + selected, Toast.LENGTH_SHORT).show()
                        );
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewTaskActivity.class);
            intent.putExtra("taskToView", new Gson().toJson(taskList.get(position)));
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
    private void showExtendDialog(Context context, Task task) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_extend_task, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        TextView tvTaskName = view.findViewById(R.id.tvTaskName);
        EditText etExtendReason = view.findViewById(R.id.etExtendReason);
        EditText etStartTime = view.findViewById(R.id.etStartTime);
        EditText etEndTime = view.findViewById(R.id.etEndTime);
        EditText etReminderTime = view.findViewById(R.id.etReminderTime);
        Button btnSave = view.findViewById(R.id.btnSaveExtend);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        tvTaskName.setText(task.getName());
        etStartTime.setText(task.getStartTime());
        etEndTime.setText(task.getEndTime());
        etReminderTime.setText(task.getReminderTime());

        View.OnClickListener timePicker = v -> {
            EditText target = (EditText) v;
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(context, (view1, y, m, d) -> {
                new TimePickerDialog(context, (view2, h, min) -> {
                    calendar.set(y, m, d, h, min);
                    target.setText(dateFormat.format(calendar.getTime()));
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        };

        etStartTime.setOnClickListener(timePicker);
        etEndTime.setOnClickListener(timePicker);
        etReminderTime.setOnClickListener(timePicker);

        btnSave.setOnClickListener(v -> {
            String newReason = etExtendReason.getText().toString().trim();
            if (newReason.isEmpty()) {
                Toast.makeText(context, "Extend reason must fill", Toast.LENGTH_SHORT).show();
                return;
            }

            String combinedReason = task.getExtendReason();
            if (!combinedReason.isEmpty()) combinedReason += "\n";
            combinedReason += newReason;
            combinedReason = combinedReason.replace("\n", " \\n ");

            task.setExtendReason(combinedReason);
            task.setStartTime(etStartTime.getText().toString());
            task.setEndTime(etEndTime.getText().toString());
            task.setReminderTime(etReminderTime.getText().toString());
            task.setNumOfReminder(10);

            FirebaseDatabase.getInstance().getReference("tasks")
                    .child(task.getId())
                    .setValue(task)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Extend updated", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

}
