package com.example.taskapp.works;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.taskapp.R;
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
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TaskReminderWorker extends Worker {

    private DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
    private DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("members");

    public TaskReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        checkAndNotifyTasks();
        return Result.success();
    }

    private void checkAndNotifyTasks() {
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                Date now = new Date();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task == null || task.getStatus() == TaskStatus.DONE
                            || task.getStatus() == TaskStatus.OVERDUE) continue;

                    try {
                        Date reminderDate = sdf.parse(task.getReminderTime());
                        Date endDate = sdf.parse(task.getEndTime());

                        if (reminderDate != null && now.after(reminderDate) && task.getNumOfReminder() > 0) {
                            sendNotification(task);

                            task.setNumOfReminder(task.getNumOfReminder() - 1);
                            taskRef.child(task.getId()).setValue(task);
                        }

                        // overdue
                        if (endDate != null && now.after(endDate)) {
                            task.setStatus(TaskStatus.OVERDUE);
                            taskRef.child(task.getId()).setValue(task);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void sendNotification(Task task) {
        memberRef.child(task.getMember1Id()).addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Member member = snapshot.getValue(Member.class);
                String assignee = member != null ? member.getName() : "unknown";

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                    Date now = new Date();
                    Date end = sdf.parse(task.getEndTime());

                    long diffMs = end.getTime() - now.getTime();
                    long days = TimeUnit.MILLISECONDS.toDays(diffMs);
                    long hours = TimeUnit.MILLISECONDS.toHours(diffMs) % 24;

                    String content = "remind " + days + " days " + hours + " h\nManager: " + assignee;

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "TASK_CHANNEL")
                            .setSmallIcon(R.drawable.ic_add_task)
                            .setContentTitle("Remind: " + task.getName())
                            .setContentText(content)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                    manager.notify(task.getId().hashCode(), builder.build());

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
