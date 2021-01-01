module MyProductivity {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;


    opens planner;
    opens planner.task;
    opens planner.controller;
    opens planner.functionalities;
}