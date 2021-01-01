package planner.task;

import java.time.LocalDate;
import java.util.Arrays;

public class NewTasks {
    private String newTaskName;
    private String goalChoice;
    private Double goalHours;
    private LocalDate pickedDate;

    public NewTasks() {
    }

    public enum newTaskChoice {
        NONE("None"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        TILL_DATE("Till date");

        public String chosenTaskGoal;

        newTaskChoice(String choice) {
            chosenTaskGoal = choice;
        }
    }

    public NewTasks(String newTaskName, String goalChoice, Double goalHours, LocalDate pickedDate) {
        this.newTaskName = newTaskName;
        this.goalChoice = goalChoice;
        this.goalHours = goalHours;
        this.pickedDate = pickedDate;
    }

    public String getNewTaskName() {
        return newTaskName;
    }

    public void setNewTaskName(String newTaskName) {
        this.newTaskName = newTaskName;
    }

    public String getGoalChoice() {
        return goalChoice;
    }

    public void setGoalChoice(String goalChoice) {
        this.goalChoice = goalChoice;
    }

    public Double getGoalHours() {
        return goalHours;
    }

    public void setGoalHours(Double goalHours) {
        this.goalHours = goalHours;
    }

    public LocalDate getPickedDate() {
        return pickedDate;
    }

    public void setPickedDate(LocalDate pickedDate) {
        this.pickedDate = pickedDate;
    }
}
