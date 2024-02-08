package com.example.task;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.task.model.TaskModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Typeface;
import android.view.Gravity;

public class AddTaskActivity extends AppCompatActivity {

    EditText etTaskInput;
    Button saveBtn;
    FirebaseFirestore db;
    String TAG="task";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        getSupportActionBar().setTitle("Add Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        saveBtn=findViewById(R.id.taskSaveBtn);
        etTaskInput=findViewById(R.id.inputTaskName);

        db = FirebaseFirestore.getInstance();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String taskName=etTaskInput.getText().toString().trim();
                if(taskName!=null)
                {
                    TaskModel taskModel=new TaskModel("",taskName,"To-Do", FirebaseAuth.getInstance().getUid());
                    db.collection("tasks").add(taskModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());

                                    findViewById(R.id.containerLL).setVisibility(View.VISIBLE);
                                    findViewById(R.id.addTaskLayout).setVisibility(View.GONE);



                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });

                }
            }
        });






    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}