package planner;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ThisWeekTasks extends TodayTasks {

    protected String goalDoneThisWeek;
    protected String goalLeftThisWeek;



    public ThisWeekTasks() {
        super();
        this.goalDoneThisWeek = getGoalDoneThisWeek();
        this.goalLeftThisWeek = getGoalLeftThisWeek();
    }

    public String getGoalDoneThisWeek() {
        return this.goalDoneThisWeek;
    }

    public void setGoalDoneThisWeek() {
        if ( !( this.goalChoice.get().equals(NewTasks.newTaskChoice.NONE.chosenTaskGoal) ) ) {

            if (        this.goalChoice.get().equals(NewTasks.newTaskChoice.DAILY.chosenTaskGoal) ) {
                this.goalDoneThisWeek = ( Math.round(( (double) this.taskDoneThisPeriod.get() / this.goalDuration.get() / 7 ) * 100D) ) + "%" ;
                setGoalLeftThisWeek(this.goalDuration.get() * 7 - this.taskDoneThisPeriod.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.WEEKLY.chosenTaskGoal) ) {
                this.goalDoneThisWeek = ( Math.round((double) this.taskDoneThisPeriod.get() / ((double) this.goalDuration.get() ) * 100D) ) + "%";
                setGoalLeftThisWeek( this.goalDuration.get() - this.taskDoneThisPeriod.get());

            } else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.MONTHLY.chosenTaskGoal) ) {
                this.goalDoneThisWeek = ( Math.round((double) this.taskDoneThisPeriod.get() / ( this.goalDuration.get() / 4D ) * 100D) ) + "%";
                setGoalLeftThisWeek(this.goalDuration.get() / 4 - this.taskDoneThisPeriod.get());

            } else {
                Calendar calTodayPlus7 = new CalendarDate(6).getOffsettedCalendar();
                long leftDurationPlus7 = this.goalDuration.get() - this.taskDoneTillThisPeriod.get();
                long leftDaysPlus7 = (this.goalDate.get() - calTodayPlus7.getTimeInMillis() ) / 86400000L;
                long averageShouldBeGoalDone = leftDurationPlus7 / leftDaysPlus7;
                setGoalLeftThisWeek(averageShouldBeGoalDone*7 - this.taskDoneThisPeriod.get());

                this.goalDoneThisWeek = ( this.taskDoneThisPeriod.get() * 100 / (averageShouldBeGoalDone*7) ) + "%";
            }

        } else {
            this.goalDoneThisWeek = "-";
            setGoalLeftThisWeek(0);
        }
    }

    public String getGoalLeftThisWeek() {
        return this.goalLeftThisWeek;
    }

    public void setGoalLeftThisWeek(long goalLeftThisWeek) {
        if (goalLeftThisWeek>0) {
            this.goalLeftThisWeek = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(goalLeftThisWeek),
                    TimeUnit.MILLISECONDS.toMinutes(goalLeftThisWeek) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(goalLeftThisWeek)));
        } else
            this.goalLeftThisWeek = "-";
    }

    public static ThisWeekTasks merge(ThisWeekTasks first, ThisWeekTasks second) {
        first.setTaskDuration(first.getTaskDuration() + second.getTaskDuration());
        first.setTaskDoneTillThisPeriod(first.getTaskDoneTillThisPeriod() + second.getTaskDoneTillThisPeriod());
        first.setTaskDoneThisPeriod(first.getTaskDoneThisPeriod() + second.getTaskDoneThisPeriod());
        return first;
    }
}
