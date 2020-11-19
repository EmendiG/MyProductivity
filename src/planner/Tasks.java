package planner;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Tasks {
    private SimpleIntegerProperty taskID;
    private SimpleStringProperty taskName;
    private SimpleLongProperty sqlID;



    public Tasks() {
        this.taskID = new SimpleIntegerProperty();
        this.taskName = new SimpleStringProperty();
        this.sqlID = new SimpleLongProperty();

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


}
