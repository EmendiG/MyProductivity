package planner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainwindow.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MyProductivity");
        primaryStage.setScene(new Scene(root, 850, 680));
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        if(!PostgreSQLJDBC.getInstance().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        PostgreSQLJDBC.getInstance().updateEndTimeForLastTaskIfNull();
        PostgreSQLJDBC.getInstance().close();
    }

    public static void main(String[] args) {
        try {
            PostgreSQLJDBC.getInstance().open();
            PostgreSQLJDBC.getInstance().setUpTable(PostgreSQLJDBC.CREATE_TABLE_TASKS);
            PostgreSQLJDBC.getInstance().setUpTable(PostgreSQLJDBC.CREATE_TABLE_TASKNAMES);
            PostgreSQLJDBC.getInstance().setUpTable(PostgreSQLJDBC.CREATE_TABLE_GOALS);
            PostgreSQLJDBC.getInstance().deleteTasksWithoutEndTime();
            PostgreSQLJDBC.getInstance().close();


        } catch (SQLException e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }

        launch(args);
    }
}
