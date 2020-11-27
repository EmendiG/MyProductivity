package planner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ThisMonthTasks extends ThisWeekTasks {

    protected String goalDoneThisMonth;
    protected String averageTaskDoneThisMonth;

    public ThisMonthTasks() {
        super();
        this.goalDoneThisMonth = getGoalDoneThisMonth();
        this.averageTaskDoneThisMonth = getAverageTaskDoneThisMonth();
    }

    public String getGoalDoneThisMonth() {
        return this.goalDoneThisMonth;
    }

    public void setGoalDoneThisMonth() {
        if ( !( this.goalChoice.get().equals(NewTasks.newTaskChoice.NONE.chosenTaskGoal) ) ) {

            if (        this.goalChoice.get().equals(NewTasks.newTaskChoice.DAILY.chosenTaskGoal) )
                this.goalDoneThisMonth = ( Math.round(( (double) this.taskDoneThisPeriod.get() / this.goalDuration.get() / 30D ) * 100D) ) + "%" ;

            else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.WEEKLY.chosenTaskGoal) )
                this.goalDoneThisMonth = (Math.round((double) this.taskDoneThisPeriod.get() / (double) this.goalDuration.get() / (30D / 7D) * 100D)) + "%";

            else if ( this.goalChoice.get().equals(NewTasks.newTaskChoice.MONTHLY.chosenTaskGoal) )
                this.goalDoneThisMonth = ( Math.round((double) this.taskDoneThisPeriod.get() / ( this.goalDuration.get()  ) * 100D) ) + "%";

            else {
                Calendar calTodayPlus29 = new CalendarDate(29).getOffsettedCalendar();
                long leftDurationPlus30 = this.goalDuration.get() - this.taskDoneTillThisPeriod.get();
                long leftDaysPlus30 = (this.goalDate.get() - calTodayPlus29.getTimeInMillis() ) / 86400000L;
                long averageShouldBeGoalDone;

                if (leftDaysPlus30 > 29)
                    averageShouldBeGoalDone = leftDurationPlus30 / leftDaysPlus30;
                else
                    averageShouldBeGoalDone = leftDurationPlus30 / 30;
                this.goalDoneThisMonth = ( this.taskDoneThisPeriod.get() * 100 / (averageShouldBeGoalDone*30) ) + "%";
            }

        } else {
            this.goalDoneThisMonth = "-";
        }
    }

    public String getAverageTaskDoneThisMonth() {
        return averageTaskDoneThisMonth;
    }

    public void setAverageTaskDoneThisMonth() {
        LocalDate firstDate = LocalDate.now().minusDays(5);
        if (!this.getHowLongTaskDoneADay().keySet().isEmpty())
            firstDate = Collections.min(this.getHowLongTaskDoneADay().keySet());
        long daysPast = ChronoUnit.DAYS.between(firstDate, LocalDate.now());
        long average;
        if (daysPast < 30)
            average = this.taskDoneThisPeriod.get() / daysPast;
        else
            average = this.taskDoneThisPeriod.get() / 30;
        this.averageTaskDoneThisMonth = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(average),
                TimeUnit.MILLISECONDS.toMinutes(average) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(average)));
    }

    public static <T extends ThisWeekTasks> T merge(T first, T second) {
        first.setTaskDuration(first.getTaskDuration() + second.getTaskDuration());
        first.setTaskDoneTillThisPeriod(first.getTaskDoneTillThisPeriod() + second.getTaskDoneTillThisPeriod());
        first.setTaskDoneThisPeriod(first.getTaskDoneThisPeriod() + second.getTaskDoneThisPeriod());
        first.putALLHowLongTaskDoneADay(first.sumALLHowLongTaskDoneADay(second.getHowLongTaskDoneADay()));
        return first;
    }
}
