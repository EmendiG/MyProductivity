package planner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.SQLException;


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
        if(!Postgresql.getInstance().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Postgresql.getInstance().updateEndTimeForLastTaskIfNull();
        Postgresql.getInstance().close();
    }

    public static void main(String[] args) {
        try {
            Postgresql.getInstance().open();
            Postgresql.getInstance().setUpTable(Postgresql.CREATE_TABLE_TASKS);
            Postgresql.getInstance().setUpTable(Postgresql.CREATE_TABLE_TASKNAMES);
            Postgresql.getInstance().setUpTable(Postgresql.CREATE_TABLE_GOALS);
            Postgresql.getInstance().deleteTasksWithoutEndTime();
            Postgresql.getInstance().close();


        } catch (SQLException e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }

        launch(args);
    }
}
