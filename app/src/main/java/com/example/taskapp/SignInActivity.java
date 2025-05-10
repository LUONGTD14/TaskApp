package com.example.taskapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.taskapp.databinding.ActivitySinginBinding;
import com.example.taskapp.model.Member;
import com.example.taskapp.utils.PasswordUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SignInActivity extends AppCompatActivity {
    private ActivitySinginBinding binding;

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
            showDialogChangePassword();
        });

        binding.txtForgotPassword.setOnClickListener(v -> {
            showDialogChangePassword();
        });
    }

    private void showDialogChangePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Authentication your information");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reset_password, null);
        EditText edtKnoxId = dialogView.findViewById(R.id.edtKnoxId);
        EditText edtEmail = dialogView.findViewById(R.id.edtEmail);

        builder.setView(dialogView);
        builder.setPositiveButton("Xác nhận", (dialog, which) -> {
            String knoxId = edtKnoxId.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();

            verifyKnoxIdAndEmail(knoxId, email);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void verifyKnoxIdAndEmail(String knoxId, String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("members");
        usersRef.orderByChild("knoxId").equalTo(knoxId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Member m = ds.getValue(Member.class);
                    if (m != null && m.getEmail().equalsIgnoreCase(email)) {
                        found = true;
                        sendEmailToResetPassword(m.getEmail(), m.getId());
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(getApplicationContext(), "Knox ID or Email invalid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailToResetPassword(String recipientEmail, String userId) {
        String newPassword = generateRandomPassword(8);

        String subject = "Reset password TaskApp";
        String body = "New password: " + newPassword;

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("members")
                .child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("password", PasswordUtils.hashPassword(newPassword));
        updates.put("firstLogin", true);

        userRef.updateChildren(updates)
                .addOnSuccessListener(unused -> {
                    new Thread(() -> {
                        try {
                            sendEmailViaSMTP(recipientEmail, subject, body);
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Please check your email", Toast.LENGTH_LONG).show()
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                            runOnUiThread(() ->
                                    Toast.makeText(this, "Send email fail: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                        }
                    }).start();
                });

    }

    private void sendEmailViaSMTP(String recipient, String subject, String body) throws MessagingException {
        final String senderEmail = "luongtd14@gmail.com";
        // https://myaccount.google.com/apppasswords
        // App → Other → "TaskApp"
        // Generate → Copy
        final String senderPassword = "zzqa sxap onqz ccik"; // App Password

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

    private String generateRandomPassword(int length) {
        String allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(allowedChars.length());
            password.append(allowedChars.charAt(index));
        }

        return password.toString();
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
                                        binding.editKnoxId.setText("");
                                        binding.editPassword.setText("");
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