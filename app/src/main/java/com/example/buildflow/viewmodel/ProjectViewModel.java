package com.example.buildflow.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.buildflow.model.Project;
import com.example.buildflow.model.ProjectRepository;

public class ProjectViewModel extends AndroidViewModel {

    private ProjectRepository repository;
    private LiveData<Boolean> projectCreationLiveData;

    public ProjectViewModel(@NonNull Application application) {
        super(application);
        repository = new ProjectRepository();
        projectCreationLiveData = repository.getProjectCreationLiveData();
    }

    public LiveData<Boolean> getProjectCreationLiveData() {
        return projectCreationLiveData;
    }

    public void addProject(Project project) {
        repository.createNewProject(project);
    }
}