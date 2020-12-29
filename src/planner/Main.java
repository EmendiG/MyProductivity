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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("controller/mainwindow.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("MyProductivity");
        primaryStage.setScene(new Scene(root, 850, 680));
        primaryStage.show();
    }

    @Override
    public void init() throws Exception {
        super.init();
        if(!PostgresDao.getInstance().open()) {
            System.out.println("FATAL ERROR: Couldn't connect to database");
            Platform.exit();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        PostgresDao.getInstance().updateEndTimeForLastTaskIfNull();
        PostgresDao.getInstance().close();
    }

    public static void main(String[] args) {
        try {
            PostgresDao.getInstance().open();
            PostgresDao.getInstance().setUpTable(PostgresDao.CREATE_TABLE_TASKS);
            PostgresDao.getInstance().setUpTable(PostgresDao.CREATE_TABLE_TASKNAMES);
            PostgresDao.getInstance().setUpTable(PostgresDao.CREATE_TABLE_GOALS);
            PostgresDao.getInstance().deleteTasksWithoutEndTime();
            PostgresDao.getInstance().close();


        } catch (SQLException e) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }

        launch(args);
    }
}
