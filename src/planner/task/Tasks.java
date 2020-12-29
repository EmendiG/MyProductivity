package planner.task;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Tasks {
    protected SimpleIntegerProperty taskID;
    protected SimpleStringProperty taskName;
    protected SimpleLongProperty sqlID;


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
