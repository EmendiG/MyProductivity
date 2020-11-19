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
    private SimpleLongProperty taskDuration;
    private SimpleLongProperty goalDuration;
    private SimpleStringProperty goalChoice;
    private SimpleLongProperty goalDate;

    private String taskDurationInString;
    private String goalDoneToday;
    private String goalLeftToday;
    private SimpleLongProperty taskDoneTillToday;
    private SimpleLongProperty taskDoneToday;


    public Tasks() {
        this.taskID = new SimpleIntegerProperty();
        this.taskName = new SimpleStringProperty();
        this.sqlID = new SimpleLongProperty();
        this.taskDuration = new SimpleLongProperty();



        this.goalChoice = new SimpleStringProperty();
        this.goalDate = new SimpleLongProperty();

        this.taskDurationInString = getTaskDurationInString();
        this.goalDuration = new SimpleLongProperty();
        this.goalDoneToday = getGoalDoneToday();
        this.goalLeftToday = getGoalLeftToday();

        this.taskDoneTillToday = new SimpleLongProperty();
        this.taskDoneToday = new SimpleLongProperty();
    }

    public static Tasks merge(Tasks first, Tasks second) {
        first.setTaskDuration(first.getTaskDuration() + second.getTaskDuration());
        first.setTaskDoneTillToday(first.getTaskDoneTillToday() + second.getTaskDoneTillToday());
        first.setTaskDoneToday(first.getTaskDoneToday() + second.getTaskDoneToday());
        return first;
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

    public long getGoalDate() {
        return goalDate.get();
    }

    public void setGoalDate(long goalDate) {
        this.goalDate.set(goalDate);
    }

    public String getTaskDurationInString() {
        return this.taskDurationInString;
    }

    public void setTaskDurationInString(Long longTaskDuration) {
        this.taskDurationInString = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(longTaskDuration),
                TimeUnit.MILLISECONDS.toMinutes(longTaskDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(longTaskDuration)));
    }

    public String getGoalDoneToday() {
        return this.goalDoneToday;
    }

    public String getGoalLeftToday() {
        return this.goalLeftToday;
    }

    public long getTaskDoneTillToday() {
        return this.taskDoneTillToday.get();
    }

    public void setTaskDoneTillToday(long taskDoneTillToday) {
        this.taskDoneTillToday.set(taskDoneTillToday);
    }

    public long getTaskDoneToday() {
        return this.taskDoneToday.get();
    }

    public void setTaskDoneToday(long taskDoneToday) {
        this.taskDoneToday.set(taskDoneToday);
    }


    public void setGoalDoneToday() {
        if ( !( this.goalChoice.get().equals(NewTasks.newTaskChoice.NONE.chosenTaskGoal) ) ) {

            if (        this.goalChoice.get().equals(NewTasks.newTaskChoice.DAILY.chosenTaskGoal) ) {
                this.goalDoneToday = String.valueOf( Math.round((double) this.taskDoneToday.get() / this.goalDuration.get() * 100D) ) ;
                setGoalLeftToday(this.goalDuration.get() - this.taskDoneToday.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.WEEKLY.chosenTaskGoal) ) {
                this.goalDoneToday = String.valueOf( Math.round((double) this.taskDoneToday.get() /  ( (double) this.goalDuration.get() / 7D ) * 100D) );
                setGoalLeftToday( this.goalDuration.get() / 7 - this.taskDoneToday.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.MONTHLY.chosenTaskGoal) ) {
                this.goalDoneToday = String.valueOf( Math.round((double) this.taskDoneToday.get() / ( this.goalDuration.get() / 31D ) * 100D) );
                setGoalLeftToday(this.goalDuration.get() / 31 - this.taskDoneToday.get());

            } else {

                Calendar calEnd = new GregorianCalendar();
                calEnd.setTime(new Date());
                calEnd.set(Calendar.DAY_OF_YEAR, calEnd.get(Calendar.DAY_OF_YEAR));
                calEnd.set(Calendar.HOUR_OF_DAY, 0);
                calEnd.set(Calendar.MINUTE, 0);
                calEnd.set(Calendar.SECOND, 0);
                calEnd.set(Calendar.MILLISECOND, 0);

                long leftHoursTillToday = this.goalDuration.get() - this.taskDoneTillToday.get();
                long leftDays = (this.goalDate.get() - calEnd.getTimeInMillis() ) / 86400000L;
                setGoalLeftToday((leftHoursTillToday)/leftDays - taskDoneToday.get());

                this.goalDoneToday = String.valueOf( this.taskDoneToday.get()*100/(leftHoursTillToday/leftDays) );
            }

        } else {
            this.goalDoneToday = "-";
        }
    }

    public void setGoalLeftToday(long goalLeftToday) {
        this.goalLeftToday = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(goalLeftToday),
                TimeUnit.MILLISECONDS.toMinutes(goalLeftToday) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(goalLeftToday)));
    }

    @Override
    public String toString() {
        return ""+ taskName.get() + " " + String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(taskDuration.get()),
                TimeUnit.MILLISECONDS.toMinutes(taskDuration.get()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(taskDuration.get()))); //         return ""+ taskName.get() ; //
    }
}
