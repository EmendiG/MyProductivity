package planner.task;

import javafx.beans.property.SimpleStringProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class AllTimeTasks extends ThisMonthTasks {
    protected Integer daysTaskPerformed;
    protected final SimpleStringProperty goalDateInString;
    protected final SimpleStringProperty goalDurationInString;
    protected String averageTaskDoneAllTime;

    public AllTimeTasks() {
        super();
        this.daysTaskPerformed = getDaysTaskPerformed();
        this.goalDateInString = new SimpleStringProperty();
        this.goalDurationInString = new SimpleStringProperty();
    }

    public Integer getDaysTaskPerformed() {
        return daysTaskPerformed;
    }
    public void setDaysTaskPerformed() {
        int daysDone = 0;
        if (!this.getHowLongTaskDoneADay().keySet().isEmpty()) {
            daysDone = this.getHowLongTaskDoneADay().keySet().size();
        }
        this.daysTaskPerformed = daysDone;
    }

    public void setGoalDateInString(long goalDate) {
        if (goalDate != 0)
            this.goalDateInString.set(Instant.ofEpochMilli(goalDate).atZone(ZoneId.systemDefault()).toLocalDate().toString() );
    }
    public String getGoalDateInString() {
        return goalDateInString.get();
    }

    public void setGoalDurationInString(long goalDuration) {
        if (goalDuration>0) {
            this.goalDurationInString.set(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(goalDuration),
                    TimeUnit.MILLISECONDS.toMinutes(goalDuration) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(goalDuration))));
        } else
            this.goalDurationInString.set("-");
    }
    public String getGoalDurationInString() {
        return goalDurationInString.get();
    }

    public String getAverageTaskDoneAllTime() {
        return averageTaskDoneAllTime;
    }
    public void setAverageTaskDoneAllTime() {
        long averageTaskDoneDaily = 0;
        if (this.daysTaskPerformed != 0)
            averageTaskDoneDaily = this.taskDuration.get()/this.daysTaskPerformed;
        this.averageTaskDoneAllTime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(averageTaskDoneDaily),
                TimeUnit.MILLISECONDS.toMinutes(averageTaskDoneDaily) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(averageTaskDoneDaily)));
    }
}
