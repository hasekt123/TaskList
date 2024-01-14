package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> tasks;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private static final String PREFS_NAME = "ToDoListPrefs";
    private static final String TASKS_KEY = "Tasks";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tasks = new ArrayList<>();
        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskAdapter = new TaskAdapter(tasks, new TaskAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                showDeleteConfirmationDialog(position);
            }
        });
        taskRecyclerView.setAdapter(taskAdapter);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        loadTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();
    }

    private void addTask() {
        EditText taskEditText = findViewById(R.id.taskEditText);
        String task = taskEditText.getText().toString();
        if (!task.isEmpty()) {
            tasks.add(task);
            taskAdapter.notifyItemInserted(tasks.size() - 1);
            taskEditText.setText("");
        }
    }

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>(tasks);
        editor.putStringSet(TASKS_KEY, set);
        editor.apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> set = prefs.getStringSet(TASKS_KEY, new HashSet<String>());
        tasks.clear();
        tasks.addAll(set);
        taskAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmationDialog(final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Odstranit úkol")
                .setMessage("Opravdu chcete odstranit tento úkol?")
                .setPositiveButton("Ano", (dialog, which) -> removeTask(position))
                .setNegativeButton("Ne", null)
                .show();
    }

    private void removeTask(int position) {
        if (position < tasks.size()) {
            tasks.remove(position);
            taskAdapter.notifyItemRemoved(position);
            taskAdapter.notifyItemRangeChanged(position, tasks.size());
            saveTasks();
        }
    }

    private static class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
        private ArrayList<String> localTasks;
        private OnItemLongClickListener longClickListener;

        public interface OnItemLongClickListener {
            void onItemLongClicked(int position);
        }

        TaskAdapter(ArrayList<String> tasks, OnItemLongClickListener listener) {
            this.localTasks = tasks;
            this.longClickListener = listener;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            String task = localTasks.get(position);
            holder.taskTextView.setText(task);
        }

        @Override
        public int getItemCount() {
            return localTasks.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView taskTextView;

            ViewHolder(View itemView) {
                super(itemView);
                taskTextView = itemView.findViewById(R.id.taskTextView);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            longClickListener.onItemLongClicked(position);
                        }
                        return true;
                    }
                });
            }
        }
    }
}