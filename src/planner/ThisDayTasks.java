package planner;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ThisDayTasks extends Tasks {
    protected final SimpleStringProperty goalChoice;
    protected final SimpleLongProperty goalDate;

    protected final SimpleLongProperty goalDuration;
    protected String goalDoneToday;
    protected String goalLeftToday;

    protected final SimpleLongProperty taskDuration;
    protected final SimpleStringProperty taskDurationInString;
    protected final SimpleLongProperty taskDoneTillThisPeriod;
    protected final SimpleLongProperty taskDoneThisPeriod;

    public ThisDayTasks() {
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
        this.taskDoneTillThisPeriod = new SimpleLongProperty();
        this.taskDoneThisPeriod = new SimpleLongProperty();
    }

    // constructor for MyAnalysis -> Tasks ChoiceBox
    public ThisDayTasks(String tempName) {
        this();
        super.taskName.set(tempName);
    }
    // enum for MyAnalysis -> Tasks ChoiceBox
    public enum TodayTasksChoice {
        ALLTASKS;

        @Override
        public String toString() {
            return "ALL TASKS";
        }
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

    public long getTaskDoneTillThisPeriod() {
        return this.taskDoneTillThisPeriod.get();
    }

    public void setTaskDoneTillThisPeriod(long taskDoneTillThisPeriod) {
        this.taskDoneTillThisPeriod.set(taskDoneTillThisPeriod);
    }

    public long getTaskDoneThisPeriod() {
        return this.taskDoneThisPeriod.get();
    }

    public void setTaskDoneThisPeriod(long taskDoneThisPeriod) {
        this.taskDoneThisPeriod.set(taskDoneThisPeriod);
    }

    public void setGoalDoneToday() {
        if ( !( this.goalChoice.get().equals(NewTasks.newTaskChoice.NONE.chosenTaskGoal) ) ) {

            if (        this.goalChoice.get().equals(NewTasks.newTaskChoice.DAILY.chosenTaskGoal) ) {
                this.goalDoneToday = ( Math.round(( (double) this.taskDoneThisPeriod.get() / this.goalDuration.get() ) * 100D) ) + "%" ;
                setGoalLeftToday(this.goalDuration.get() - this.taskDoneThisPeriod.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.WEEKLY.chosenTaskGoal) ) {
                this.goalDoneToday = ( Math.round((double) this.taskDoneThisPeriod.get() / ((double) this.goalDuration.get() / 7D) * 100D) ) + "%";
                setGoalLeftToday( this.goalDuration.get() / 7 - this.taskDoneThisPeriod.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.MONTHLY.chosenTaskGoal) ) {
                this.goalDoneToday = ( Math.round((double) this.taskDoneThisPeriod.get() / ( this.goalDuration.get() / 31D ) * 100D) ) + "%";
                setGoalLeftToday(this.goalDuration.get() / 31 - this.taskDoneThisPeriod.get());

            } else {
                Calendar calEnd = new CalendarDate(0).getOffsettedCalendar();
                long leftDays = (this.goalDate.get() - calEnd.getTimeInMillis() ) / 86400000L;
                long leftHoursTillToday = this.goalDuration.get() - this.taskDoneTillThisPeriod.get();
                if (leftDays > 0 ) {
                    setGoalLeftToday((leftHoursTillToday) / leftDays - taskDoneThisPeriod.get());
                    this.goalDoneToday = ( this.taskDoneThisPeriod.get()*100/(leftHoursTillToday/leftDays) ) + "%";
                } else {
                    setGoalLeftToday((leftHoursTillToday) - taskDoneThisPeriod.get());
                    this.goalDoneToday = "NO TIME LEFT";
                }
            }

        } else {
            this.goalDoneToday = "-";
            setGoalLeftToday(0);
        }
    }

    public void setGoalLeftToday(long goalLeftToday) {
        if (goalLeftToday>0) {
            this.goalLeftToday = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(goalLeftToday),
                    TimeUnit.MILLISECONDS.toMinutes(goalLeftToday) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(goalLeftToday)));
        } else
            this.goalLeftToday = "-";
    }

    @Override
    public String toString() {
        return ""+ super.getTaskName() + " " + super.getTaskID() + " " + String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(taskDuration.get()),
                TimeUnit.MILLISECONDS.toMinutes(taskDuration.get()) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(taskDuration.get())));
    }
}
