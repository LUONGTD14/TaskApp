package com.example.taskapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.taskapp.databinding.ActivityMainBinding;
import com.example.taskapp.dialogs.AddCategoryDialog;
import com.example.taskapp.dialogs.AddMemberDialog;
import com.example.taskapp.fragment.DoneOverdueTaskFragment;
import com.example.taskapp.fragment.InProgressTaskFragment;
import com.example.taskapp.works.TaskReminderWorker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton fabAddMember, fabAddCategory, fabAddTask;
    private AddMemberDialog addMemberDialog;
    private AddCategoryDialog addCategoryDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        addMemberDialog = new AddMemberDialog(this);
        addCategoryDialog = new AddCategoryDialog(this);

        binding.fabAddMember.setOnClickListener(v -> {
            addMemberDialog.showAddMemberDialog();
        });

        binding.fabAddCategory.setOnClickListener(v -> {
            addCategoryDialog.showAddCategoryDialog();
        });

        binding.fabAddTask.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTaskActivity.class));
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "TASK_CHANNEL",
                    "Task Reminder Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                TaskReminderWorker.class,
                30, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TaskReminderWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest);


    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "In progress Task" : "Done/Overdue Task");
        }).attach();
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return position == 0 ? new InProgressTaskFragment() : new DoneOverdueTaskFragment();
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

}