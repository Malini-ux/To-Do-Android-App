package com.example.task.model;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MutableLiveData;

public class TaskViewModel extends ViewModel {
    MutableLiveData<Boolean> taskAddedLiveData = new MutableLiveData<>();
}
