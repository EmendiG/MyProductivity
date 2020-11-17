package planner;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tasks {
    private SimpleIntegerProperty taskID;
    private SimpleStringProperty taskName;
    private SimpleLongProperty sqlID;
    private SimpleLongProperty taskDuration;
    private SimpleLongProperty goalDuration;
    private SimpleStringProperty goalChoice;
    private SimpleLongProperty goalTillDate;

    public Tasks() {
        this.taskID = new SimpleIntegerProperty();
        this.taskName = new SimpleStringProperty();
        this.sqlID = new SimpleLongProperty();
        this.taskDuration = new SimpleLongProperty();
        this.goalDuration = new SimpleLongProperty();
        this.goalChoice = new SimpleStringProperty();
        this.goalTillDate = new SimpleLongProperty();
    }

    public int getTaskID() {
        return taskID.get();
    }

    public void setTaskID(int taskID) {
        this.taskID.set(taskID);
    }

    public String getTaskName() {
        return taskName.get();
    }

    public void setTaskName(String taskName) {
        this.taskName.set(taskName);
    }

    public long getID() {
        return sqlID.get();
    }

    public void setID(long ID) {
        this.sqlID.set(ID);
    }

    public long getTaskDuration() {
        return taskDuration.get();
    }

    public void setTaskDuration(long taskDuration) {
        this.taskDuration.set(taskDuration);
    }

    public long getGoalDuration() {
        return goalDuration.get();
    }

    public void setGoalDuration(long goalDuration) {
        this.goalDuration.set(goalDuration);
    }

    public String getGoalChoice() {
        return goalChoice.get();
    }

    public void setGoalChoice(String goalChoice) {
        this.goalChoice.set(goalChoice);
    }

    public long getGoalTillDate() {
        return goalTillDate.get();
    }

    public void setGoalTillDate(long goalTillDate) {
        this.goalTillDate.set(goalTillDate);
    }

    @Override
    public String toString() {
        return ""+ taskName.get();
    }

    public static Set<Tasks> getUniqueTasksByTaskID(List<Tasks> tasks) {
        Set<Tasks> tasksSet = new HashSet<>();
        List taskIDs = new ArrayList();
        for(Tasks task: tasks) {
            if (!taskIDs.contains(task.getTaskID())) {
                tasksSet.add(task);
                taskIDs.add(task.getTaskID());
            }
        }
        return tasksSet;
    }
}
