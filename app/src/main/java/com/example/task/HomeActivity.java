package com.example.task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.task.model.TaskModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

//Share
import java.io.FileOutputStream;
import android.net.Uri;
import java.io.File;
import java.io.IOException;
import androidx.core.content.FileProvider;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Element;


public class HomeActivity extends AppCompatActivity {

    RecyclerView taskRv;
    ArrayList<TaskModel> dataList=new ArrayList<>();
    TaskListAdapter taskListAdapter;
    FirebaseFirestore db;
    String TAG="Homepage query docs";
    TextView userNameTv;
    CircleImageView userImageIv;
    SearchView searchView;
    Button btnShare;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().hide();
        db=FirebaseFirestore.getInstance();
        taskRv=findViewById(R.id.taskListRv);
        userImageIv=findViewById(R.id.userProfileIv);
        searchView=findViewById(R.id.searchview);

        //Click to Share
        findViewById(R.id.shareFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareReport();
            }
        });

        //Click to Sign out
        userImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });
        Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(userImageIv);

        //Initialize Adapter and RecyclerView
        taskListAdapter=new TaskListAdapter(dataList);
        RecyclerView.LayoutManager layoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        taskRv.setLayoutManager(layoutManager);
        taskRv.setAdapter(taskListAdapter);


        //Click to add Task
        findViewById(R.id.addTaskFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show the bottom sheet
                AddTaskBottomSheetFragment bottomSheetFragment = new AddTaskBottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
            }
        });

        //load DB data
        loadTasks();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Clear existing data
                dataList.clear();

                // Perform search query
                db.collection("tasks")
                        .orderBy("taskName")
                        .startAt(query)
                        .endAt(query + '\uf8ff')
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        TaskModel taskModel = document.toObject(TaskModel.class);
                                        taskModel.setTaskId(document.getId());
                                        dataList.add(taskModel);
                                    }
                                    // Notify adapter of data changes
                                    taskListAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Check if the search query text is empty
                if (newText.isEmpty()) {
                    // Clear dataList and reload all tasks
                    dataList.clear();
                    loadTasks();
                } else {
                    // Perform search query
                    db.collection("tasks")
                            .orderBy("taskName")
                            .startAt(newText)
                            .endAt(newText + '\uf8ff')
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        dataList.clear(); // Clear existing data
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d(TAG, document.getId() + " => " + document.getData());
                                            TaskModel taskModel = document.toObject(TaskModel.class);
                                            taskModel.setTaskId(document.getId());
                                            dataList.add(taskModel);
                                        }
                                        // Notify adapter of data changes
                                        taskListAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
                return true;
            }

        });
    }
    //Definition for Share
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.task.fileprovider";
    private void shareReport() {
        try {
            // Create a temporary file to store the PDF
            File pdfFile = new File(getFilesDir(), "task_report.pdf");
            FileOutputStream outputStream = new FileOutputStream(pdfFile);

            // Create a Document instance
            Document document = new Document();

            // Create a PdfWriter instance and associate it with the Document
            PdfWriter.getInstance(document, outputStream);

            // Open the Document for writing
            document.open();

            // Add a title to the PDF
            Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
            Paragraph title = new Paragraph("Task Report\n\n", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add user name
            document.add(new Paragraph(String.format("Generated by: %s\n\n",
                    FirebaseAuth.getInstance().getCurrentUser().getDisplayName())));
            // Add a table for tasks and status
            PdfPTable table = new PdfPTable(2); // 2 columns
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font tableHeaderFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.BOLD);
            PdfPCell headerCell1 = new PdfPCell(new Phrase("Task", tableHeaderFont));
            PdfPCell headerCell2 = new PdfPCell(new Phrase("Status", tableHeaderFont));

            table.addCell(headerCell1);
            table.addCell(headerCell2);

            Font tableBodyFont = new Font(Font.FontFamily.TIMES_ROMAN, 12);

            for (TaskModel task : dataList) {
                PdfPCell cell1 = new PdfPCell(new Phrase(task.getTaskName(), tableBodyFont));
                PdfPCell cell2 = new PdfPCell(new Phrase(task.getStatus(), tableBodyFont));

                table.addCell(cell1);
                table.addCell(cell2);
            }

            document.add(table);
            // Close the Document
            document.close();
            outputStream.close();

            // Create an Intent to share the PDF file
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");

            // Create a content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, pdfFile);

            // Grant temporary read permission to the content URI
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

            // Start the activity to show the share dialog
            startActivity(Intent.createChooser(shareIntent, "Share Task Report"));

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }

    }

    //Load from DB
    public void loadTasks() {
        db.collection("tasks")
                .whereEqualTo("userId", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dataList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TaskModel taskModel = document.toObject(TaskModel.class);
                            taskModel.setTaskId(document.getId());
                            dataList.add(taskModel);
                        }
                        taskListAdapter.notifyDataSetChanged(); // Notify adapter
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }


}