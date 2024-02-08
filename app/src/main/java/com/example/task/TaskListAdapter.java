package com.example.task;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.task.model.TaskModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import android.graphics.Typeface;
import android.view.Gravity;


public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.ViewHolder> {

    private ArrayList<TaskModel> taskDataset;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskNameTv,taskStatusTv;

        LinearLayout containerLl;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            taskNameTv = (TextView) view.findViewById(R.id.taskNameTv);
            taskStatusTv = (TextView) view.findViewById(R.id.taskStatusTv);
            containerLl=(LinearLayout) view.findViewById(R.id.containerLL);
        }


    }


    public TaskListAdapter(ArrayList<TaskModel> taskDataset) {
        this.taskDataset = taskDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_task, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.taskNameTv.setText(taskDataset.get(position).getTaskName());
        viewHolder.taskStatusTv.setText(taskDataset.get(position).getTaskStatus());

        String status=taskDataset.get(position).getTaskStatus();

        if(status.toLowerCase().equals("to-do"))
        {
            viewHolder.taskStatusTv.setBackgroundColor(Color.parseColor("#FFCCCB"));
            viewHolder.taskStatusTv.setGravity(Gravity.CENTER);
            //viewHolder.taskNameTv.setTypeface(null, Typeface.BOLD);
        } else if(status.toLowerCase().equals("done"))
        {
            viewHolder.taskStatusTv.setBackgroundColor(Color.parseColor("#C7F6C7"));
            viewHolder.taskStatusTv.setGravity(Gravity.CENTER);
            //viewHolder.taskNameTv.setTypeface(null, Typeface.BOLD);
        }else{

            viewHolder.taskStatusTv.setBackgroundColor(Color.parseColor("#ffffff"));
            viewHolder.taskStatusTv.setGravity(Gravity.CENTER);
            //viewHolder.taskNameTv.setTypeface(null, Typeface.BOLD);
        }

        viewHolder.containerLl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                PopupMenu popupMenu=new PopupMenu(view.getContext(),viewHolder.containerLl );
                popupMenu.inflate(R.menu.taskmenu);
                popupMenu.show();


                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        if(menuItem.getItemId()==R.id.deleteMenu)
                        {


                            FirebaseFirestore.getInstance().collection("tasks").document(taskDataset.get(position).getTaskId()).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {

                                            Toast.makeText(view.getContext(), "Item deleted",Toast.LENGTH_SHORT).show();
                                            viewHolder.containerLl.setVisibility(View.GONE);

                                        }
                                    });


                        }
                        // When menu item is "marked done"
                        if(menuItem.getItemId()==R.id.markCompleteMenu)
                        {
                            TaskModel completedTask=taskDataset.get(position);
                            completedTask.setTaskStatus("Done");

                            //SAVE TO DATABASE
                            FirebaseFirestore.getInstance().collection("tasks").document(taskDataset.get(position).getTaskId())
                                    .set(completedTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(view.getContext(), "Task Item Marked As Completed",Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            //UPDATE TASK STATUS ON SCREEN
                            viewHolder.taskStatusTv.setBackgroundColor(Color.parseColor("#C7F6C7"));
                            viewHolder.taskStatusTv.setGravity(Gravity.CENTER);
                            //viewHolder.taskNameTv.setTypeface(null, Typeface.BOLD);
                            viewHolder.taskStatusTv.setText("Done");
                        }
                        return false;
                    }
                });





                return false;
            }
        });





    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return taskDataset.size();
    }

    public void clearAllItems(){
        taskDataset.clear();
        notifyDataSetChanged();

    }
}