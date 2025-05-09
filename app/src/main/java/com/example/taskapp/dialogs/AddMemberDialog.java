package com.example.taskapp.dialogs;

import android.content.Context;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.taskapp.R;
import com.example.taskapp.model.Member;
import com.example.taskapp.utils.PasswordUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class AddMemberDialog {
    Context context;

    public AddMemberDialog(Context context) {
        this.context = context;
    }

    public void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_member, null);
        builder.setView(view);
        builder.setTitle("Add member");

        EditText editName = view.findViewById(R.id.editName);
        EditText editKnoxId = view.findViewById(R.id.editKnoxId);
        EditText editEmail = view.findViewById(R.id.editEmail);
        EditText editPhone = view.findViewById(R.id.editPhone);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String name = editName.getText().toString().trim();
                String knoxid = editKnoxId.getText().toString().trim();
                String email = editEmail.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();

                if (name.isEmpty() || knoxid.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(context, "Please enter the information", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(context, "Email invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!phone.matches("^\\d{10}$")) {
                    Toast.makeText(context, "Phone must 10 number", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("members");

                ref.orderByChild("knoxid").equalTo(knoxid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(context, "KnoxID exits", Toast.LENGTH_SHORT).show();
                        } else {
                            String id = UUID.randomUUID().toString();
                            String inputHashed = PasswordUtils.hashPassword(knoxid);
                            Member member = new Member(id, name, knoxid, inputHashed, email, phone);
                            ref.child(id).setValue(member);
                            Toast.makeText(context, "Add member successful", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            });
        });

        dialog.show();
    }
}
