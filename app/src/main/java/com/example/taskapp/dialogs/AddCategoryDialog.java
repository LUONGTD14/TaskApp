package com.example.taskapp.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.taskapp.R;
import com.example.taskapp.model.Category;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class AddCategoryDialog {
    Context context;

    public AddCategoryDialog(Context context) {
        this.context = context;
    }

    public void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_category, null);
        builder.setView(view);
        builder.setTitle("Add Category");

        EditText editName = view.findViewById(R.id.editCategoryName);
        EditText editDescription = view.findViewById(R.id.editCategoryDescription);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Canel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button btnAdd = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnAdd.setOnClickListener(v -> {
                String name = editName.getText().toString().trim();
                String description = editDescription.getText().toString().trim();

                if (name.isEmpty() || description.isEmpty()) {
                    Toast.makeText(context, "Please enter the description", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("categories");
                String id = UUID.randomUUID().toString();
                description = description.replace("\n", " \\n ");
                Category category = new Category(id, name, description);
                ref.child(id).setValue(category)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Add category successful", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        });

        dialog.show();
    }

}
