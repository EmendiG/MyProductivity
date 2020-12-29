module MyProductivity {
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.sql;
    requires org.jfree.jfreechart;
    requires jfreechart.fx;
    requires java.base;
    requires javafx.base;


    opens planner;
    opens planner.task;
    opens planner.controller;
    opens planner.functionalities;
}