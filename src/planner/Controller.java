package planner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import javafx.beans.binding.Bindings;


public class Controller {

    @FXML
    private ListView<Tasks> tasksTable;

    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    @FXML
    private Label currentTimeLabel;
    @FXML
    private Label currentTaskLabel;

    @FXML
    private TextField filterTasksTextField;

    @FXML
    private VBox mainWindow;

    @FXML
    private TableView<Tasks> todayTableView;

    private TaskTimer taskTimer = new TaskTimer();
    private Tasks currentlyAssignedTask;


    public void initialize() {
        updateListView(0, null);
        updateDayTableView();
    }

    public void startCurrentTask() {
        final Tasks selectedTask = tasksTable.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No task was selected");
            alert.setHeaderText("Select the task from a list and then start the timer");
            alert.showAndWait();
            return;
        }
        tasksTable.setDisable(true);
        tasksTable.setStyle(" -fx-selection-bar: green; -fx-selection: green; -fx-selection-bar-non-focused: green;");
        tasksTable.setOpacity(1);
        try {
            currentlyAssignedTask = PostgreSQLJDBC.getInstance().insertTask(selectedTask);
            startButton.setDisable(true);
            currentTaskLabel.setText(selectedTask.getTaskName());
            showCurrentTimeElapsed();
            stopButton.setDisable(false);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void stopCurrentTask() {
        try {
            tasksTable.setDisable(false);
            PostgreSQLJDBC.getInstance().insertEndTime(currentlyAssignedTask.getID());
            stopButton.setDisable(true);
            currentTaskLabel.setText( "None" );
            stopCurrentTimeElapsed();
            startButton.setDisable(false);
            tasksTable.setStyle("-fx-selection-bar: lightblue; -fx-selection-bar-non-focused: lightblue;");

            // updating tables
            updateDayTableView();
            updateListView(tasksTable.getSelectionModel().getSelectedIndices().get(0), currentlyAssignedTask.getTaskName());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @FXML
    public void showCurrentTimeElapsed() {
        taskTimer.startTimer(currentTimeLabel);
    }

    @FXML
    public void stopCurrentTimeElapsed() {
        taskTimer.stopTimer();
        currentTimeLabel.setText( "None" );
    }

    @FXML
    public void showNewTaskWindow() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        dialog.setTitle("MyProductivity New Task");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("newtaskwindow.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        NewTaskController newController = fxmlLoader.getController();

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            NewTasks newTask = newController.processResults();
            if ( newTask.getNewTaskName() == null )
                showNewTaskWindow();
            else {
                List<String> listedTasknames = PostgreSQLJDBC.getInstance().getListedTasknames();
                Alert alert;
                Alert successfullyAddedTaskAlert = new Alert(Alert.AlertType.INFORMATION);
                successfullyAddedTaskAlert.setTitle("Task added");
                successfullyAddedTaskAlert.setHeaderText("Task has been added to the list");

                boolean similarTaskFound = false;
                boolean theSameTask = false;
                String similarTaskname = "";
                for (String existingTask : listedTasknames) {
                    if ( StringSimilarity.similarity(existingTask, newTask.getNewTaskName()) > 0.66 ) {
                        similarTaskFound = true;
                        similarTaskname = existingTask;
                        if ( StringSimilarity.similarity(existingTask, newTask.getNewTaskName()) == 1 )
                            theSameTask = true;
                    }
                }

                if ( similarTaskFound && ! theSameTask ) {
                    alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Task might exist");
                    alert.setHeaderText("Similar task might already exists! \nTask found = " + similarTaskname);
                    alert.setContentText("Are you sure that you want to add task?");
                    Optional<ButtonType> buttonType = alert.showAndWait();
                    if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                        PostgreSQLJDBC.getInstance().insertNewGoal(newTask);
                        successfullyAddedTaskAlert.show();
                        updateListView(tasksTable.getItems().size(), newTask.getNewTaskName());
                    }
                } else if ( ! theSameTask) {
                    PostgreSQLJDBC.getInstance().insertNewGoal(newTask);
                    successfullyAddedTaskAlert.show();
                    updateListView(tasksTable.getItems().size(), newTask.getNewTaskName());
                } else {
                    alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Task already exists");
                    alert.setHeaderText("The same task named \"" + similarTaskname + "\" found");
                    alert.setContentText("This task insertion is not possible");
                    alert.show();
                }
            }
        }
    }

    public void updateListView(int taskSelected, String currentTaskAssigned) {
        ObservableList<Tasks> observableTaskList = FXCollections.observableArrayList(
                PostgreSQLJDBC.getInstance()
                        .getSortedTaskByLengthInTimeRange(  0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000)
                        )
        );

        FilteredList<Tasks> filteredTasks = new FilteredList<>(observableTaskList);
        tasksTable.setItems(filteredTasks);

        filteredTasks.predicateProperty().bind(Bindings.createObjectBinding( () -> {
            String text = filterTasksTextField.getText();
            if ( text == null || text.isEmpty() )
                return null;
            else {
                final String lowerCase = text.toLowerCase();
                return (tasks) -> tasks.getTaskName().toLowerCase().contains(lowerCase);
            }
        }, filterTasksTextField.textProperty() ));

        tasksTable.getSelectionModel().select(taskSelected);
        int positionInt = 0;
        int positionLeft = tasksTable.getItems().size() -1;
        while ( positionLeft >=0 ) {

            if (currentTaskAssigned != null) {
                if (tasksTable.getSelectionModel().getSelectedItem().getTaskName().equals(currentTaskAssigned)) {
                    tasksTable.getSelectionModel().select(taskSelected - positionInt);
                    break;
                } else {
                    positionInt = positionInt +1;
                    positionLeft = positionLeft -1;
                    tasksTable.getSelectionModel().select(taskSelected-positionInt);
                }
            } else
                break;
        }
    }

    public void updateDayTableView() {

        ObservableList<Tasks> observableTaskList = FXCollections.observableArrayList(
                PostgreSQLJDBC.getInstance()
                        .getSortedTaskByLengthInTimeRange(  0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000)
                        )
        );
        observableTaskList.stream().forEach(x -> x.setGoalDoneToday());

        todayTableView.setItems(observableTaskList);
    }
}
