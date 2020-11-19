package planner;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class TodayTasks extends Tasks {

    private final SimpleLongProperty taskDuration;
    private final SimpleLongProperty goalDuration;
    private final SimpleStringProperty goalChoice;
    private final SimpleLongProperty goalDate;
    private final SimpleStringProperty taskDurationInString;
    private String goalDoneToday;
    private String goalLeftToday;
    private final SimpleLongProperty taskDoneTillToday;
    private final SimpleLongProperty taskDoneToday;

    public TodayTasks() {
        super();
        this.goalChoice = new SimpleStringProperty();
        this.goalDate = new SimpleLongProperty();

        // goalDuration == goalDoneToday + goalLeftToday
        this.goalDuration = new SimpleLongProperty();
        this.goalDoneToday = getGoalDoneToday();
        this.goalLeftToday = getGoalLeftToday();

        // taskDuration == taskDoneTillToday + taskDoneToday
        this.taskDuration = new SimpleLongProperty();
        this.taskDurationInString =  new SimpleStringProperty();
        this.taskDoneTillToday = new SimpleLongProperty();
        this.taskDoneToday = new SimpleLongProperty();
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
        return this.taskDurationInString.get();
    }

    public void setTaskDurationInString(Long longTaskDuration) {
        this.taskDurationInString.set( String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(longTaskDuration),
                TimeUnit.MILLISECONDS.toMinutes(longTaskDuration) -
                        TimeUnit.HOURS.toMinutes( TimeUnit.MILLISECONDS.toHours(longTaskDuration) ) )
        );
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
                this.goalDoneToday = String.valueOf( Math.round(( (double) this.taskDoneToday.get() / this.goalDuration.get() ) * 100D) ) ;
                setGoalLeftToday(this.goalDuration.get() - this.taskDoneToday.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.WEEKLY.chosenTaskGoal) ) {
                this.goalDoneToday = String.valueOf( Math.round((double) this.taskDoneToday.get() / ((double) this.goalDuration.get() / 7D) * 100D) );
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


    public static TodayTasks merge(TodayTasks first, TodayTasks second) {
        first.setTaskDuration(first.getTaskDuration() + second.getTaskDuration());
        first.setTaskDoneTillToday(first.getTaskDoneTillToday() + second.getTaskDoneTillToday());
        first.setTaskDoneToday(first.getTaskDoneToday() + second.getTaskDoneToday());
        return first;
    }


    @Override
    public String toString() {
        return ""+ super.getTaskName() + " " + super.getTaskID() + " " + String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(taskDuration.get()),
                TimeUnit.MILLISECONDS.toMinutes(taskDuration.get()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(taskDuration.get())));
    }
}
