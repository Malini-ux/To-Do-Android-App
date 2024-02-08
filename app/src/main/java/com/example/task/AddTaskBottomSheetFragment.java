package com.example.task;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.task.model.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;



public class AddTaskBottomSheetFragment extends BottomSheetDialogFragment {
    EditText etTaskInput;
    Button saveBtn;
    FirebaseFirestore db;
    String TAG = "task";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_add_task_bottom_sheet, container, false);
        db = FirebaseFirestore.getInstance();
        TextInputEditText inputTaskName = view.findViewById(R.id.inputTaskName);
        Button saveButton = view.findViewById(R.id.taskSaveBtn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String taskName = inputTaskName.getText().toString().trim();
                if (!taskName.isEmpty()) {
                    TaskModel taskModel = new TaskModel("", taskName, "To-Do", FirebaseAuth.getInstance().getUid());
                    db.collection("tasks").add(taskModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            ((HomeActivity) getActivity()).loadTasks();
                            dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
                } else {
                    // Handle the case when task name is empty
                    Toast.makeText(getContext(), "Task name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
