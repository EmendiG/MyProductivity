package planner;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ThisWeekTasks extends ThisDayTasks {

    protected String goalDoneThisWeek;
    protected String goalLeftThisWeek;
    public Map<LocalDate, Long> howLongTaskDoneADay;

    public ThisWeekTasks() {
        super();
        this.goalDoneThisWeek = getGoalDoneThisWeek();
        this.goalLeftThisWeek = getGoalLeftThisWeek();
        this.howLongTaskDoneADay = new HashMap<>();
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
                long averageShouldBeGoalDone;
                if (leftDaysPlus7 > 6) {
                    averageShouldBeGoalDone = leftDurationPlus7 / leftDaysPlus7;
                    setGoalLeftThisWeek(averageShouldBeGoalDone * 7 - this.taskDoneThisPeriod.get());
                }
                else {
                    averageShouldBeGoalDone = leftDurationPlus7 / 7;
                    setGoalLeftThisWeek(0);
                }
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

    public Map<LocalDate, Long> getHowLongTaskDoneADay() {
        return howLongTaskDoneADay;
    }

    public void setHowLongTaskDoneADay(Map<LocalDate, Long> howLongTaskDoneADay) {
        this.howLongTaskDoneADay = howLongTaskDoneADay;
    }

    public void putHowLongTaskDoneADay(LocalDate whatDate, Long howLong) {
        this.howLongTaskDoneADay.put(whatDate, howLong);
    }

    public void putALLHowLongTaskDoneADay(Map<LocalDate, Long> howLongTaskDoneADay) {
        this.howLongTaskDoneADay.putAll( howLongTaskDoneADay );
    }

    public Map<LocalDate, Long> sumALLHowLongTaskDoneADay(Map<LocalDate, Long> howLongTaskDoneADay) {
        if (!howLongTaskDoneADay.keySet().isEmpty()) {
            LocalDate key = (LocalDate) howLongTaskDoneADay.keySet().toArray()[0];
            if (this.howLongTaskDoneADay.containsKey(key))
                this.howLongTaskDoneADay.put(key, this.howLongTaskDoneADay.get(key) + howLongTaskDoneADay.get(key));
            else
                this.howLongTaskDoneADay.putAll(howLongTaskDoneADay);
        }
        return this.howLongTaskDoneADay;
    }

    public static ThisWeekTasks merge(ThisWeekTasks first, ThisWeekTasks second) {
        first.setTaskDuration(first.getTaskDuration() + second.getTaskDuration());
        first.setTaskDoneTillThisPeriod(first.getTaskDoneTillThisPeriod() + second.getTaskDoneTillThisPeriod());
        first.setTaskDoneThisPeriod(first.getTaskDoneThisPeriod() + second.getTaskDoneThisPeriod());
        first.putALLHowLongTaskDoneADay(first.sumALLHowLongTaskDoneADay(second.getHowLongTaskDoneADay()));
        return first;
    }

}
