package com.example.taskapp.works;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.example.taskapp.R;
import com.example.taskapp.enums.TaskStatus;
import com.example.taskapp.model.Member;
import com.example.taskapp.model.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskReminderWorker extends ListenableWorker {
    private final Context context;

    public TaskReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        SettableFuture<Result> future = SettableFuture.create();
        checkAndNotifyTasks(future);
        return future;
    }

    private void checkAndNotifyTasks(SettableFuture<Result> future) {
        DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks");
        DatabaseReference memberRef = FirebaseDatabase.getInstance().getReference("members");

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
                Date now = new Date();
                List<Task> tasksToNotify = new ArrayList<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Task task = child.getValue(Task.class);
                    if (task == null || task.getStatus() == TaskStatus.DONE || task.getStatus() == TaskStatus.OVERDUE)
                        continue;

                    try {
                        Date reminderDate = sdf.parse(task.getReminderTime());
                        Date endDate = sdf.parse(task.getEndTime());

                        if (reminderDate != null && now.after(reminderDate) && task.getNumOfReminder() > 0) {
                            tasksToNotify.add(task);
                            task.setNumOfReminder(task.getNumOfReminder() - 1);
                            taskRef.child(task.getId()).setValue(task);
                        }

                        if (endDate != null && now.after(endDate)) {
                            task.setStatus(TaskStatus.OVERDUE);
                            taskRef.child(task.getId()).setValue(task);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (tasksToNotify.isEmpty()) {
                    future.set(Result.success());
                    return;
                }

                AtomicInteger remaining = new AtomicInteger(tasksToNotify.size());
                for (Task task : tasksToNotify) {
                    sendNotification(task, memberRef, () -> {
                        if (remaining.decrementAndGet() == 0) {
                            future.set(Result.success());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                future.set(Result.failure());
            }
        });
    }

    private void sendNotification(Task task, DatabaseReference memberRef, Runnable onComplete) {
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

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "TASK_CHANNEL")
                            .setSmallIcon(R.drawable.ic_add_task)
                            .setContentTitle("Remind: " + task.getName())
                            .setContentText(content)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                    NotificationManagerCompat manager = NotificationManagerCompat.from(context);
                    manager.notify(task.getId().hashCode(), builder.build());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                onComplete.run(); // Đảm bảo gọi callback
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run(); // Vẫn gọi để tránh treo
            }
        });
    }

}
