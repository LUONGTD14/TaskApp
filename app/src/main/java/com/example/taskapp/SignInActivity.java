package com.example.taskapp;

import android.content.Intent;
import android.os.Bundle;

import com.example.taskapp.databinding.ActivityAddTaskBinding;
import com.example.taskapp.model.Member;
import com.example.taskapp.utils.PasswordUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskapp.databinding.ActivitySinginBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignInActivity extends AppCompatActivity {
    private ActivitySinginBinding binding;
    private EditText editKnoxId, editPassword;
    private Button btnLogin;
    private TextView txtChangePassword, txtForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySinginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> {
            String knoxid = binding.editKnoxId.getText().toString().trim();
            String pass = binding.editPassword.getText().toString().trim();
            if (knoxid.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please enter knoxId and password", Toast.LENGTH_SHORT).show();
                return;
            }
            login(knoxid, pass);
        });

        binding.txtChangePassword.setOnClickListener(v -> {
//            Intent intent = new Intent(this, ChangePasswordActivity.class);
//            startActivity(intent);
        });

        binding.txtForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng liên hệ quản trị viên để đặt lại mật khẩu.", Toast.LENGTH_LONG).show();
        });
    }

    private void login(String knoxid, String passwordInput) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members");

        ref.orderByChild("knoxId").equalTo(knoxid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                Member member = userSnapshot.getValue(Member.class);
                                String inputHashed = PasswordUtils.hashPassword(passwordInput);
                                if (member.getPassword().equals(inputHashed)) {
                                    if (member.isFirstLogin()) {
                                        promptChangePassword(userSnapshot.getKey(), member);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Password incorrect", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Username not exist", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void promptChangePassword(String memberId, Member member) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter new password");
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newPass = input.getText().toString().trim();
            if (newPass.length() < 6) {
                Toast.makeText(this, "Password very weak", Toast.LENGTH_SHORT).show();
                return;
            }

            String hashed = PasswordUtils.hashPassword(newPass);
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members").child(memberId);
            ref.child("password").setValue(hashed);
            ref.child("firstLogin").setValue(false);

            Toast.makeText(this, "Change password successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}