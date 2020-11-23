package planner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.util.StringConverter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class Controller {

    @FXML
    private VBox mainWindow;


    // Tab 1
    @FXML
    private ListView<ThisDayTasks> tasksTable;
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
    private TableView<ThisDayTasks> todayTableView;
    @FXML
    private TableView<ThisWeekTasks> weekTableView;


    private Tasks currentlyAssignedTask;
    private final TaskTimer taskTimer = new TaskTimer();

    public void initialize() {
        updateListView(0, null);
        updateTodayTableView();
        updateWeeklyTableView();
        showDataOnTab2Graphs();
    }

    public void startCurrentTask() {
        final ThisDayTasks selectedTask = tasksTable.getSelectionModel().getSelectedItem();

        if (selectedTask == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No task was selected");
            alert.setHeaderText("Select the task from a list and then start the timer");
            alert.showAndWait();
            return;
        }

        if (selectedTask.goalChoice.get().equals(NewTasks.newTaskChoice.TILL_DATE.chosenTaskGoal)
                && selectedTask.getGoalDate() < System.currentTimeMillis()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Task selection impossible");
            alert.setHeaderText("The task goal date has already passed");
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
            currentTaskLabel.setText("None");
            stopCurrentTimeElapsed();
            startButton.setDisable(false);
            tasksTable.setStyle("-fx-selection-bar: lightblue; -fx-selection-bar-non-focused: lightblue;");
            // updating tables
            updateListView(tasksTable.getSelectionModel().getSelectedIndices().get(0), currentlyAssignedTask.getTaskName());
            updateTodayTableView();
            updateWeeklyTableView();

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
        currentTimeLabel.setText("None");
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

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        NewTaskController newController = fxmlLoader.getController();

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            NewTasks newTask = newController.processResults();
            if (newTask.getNewTaskName() == null)
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
                    if (StringSimilarity.similarity(existingTask, newTask.getNewTaskName()) > 0.66) {
                        similarTaskFound = true;
                        similarTaskname = existingTask;
                        if (StringSimilarity.similarity(existingTask, newTask.getNewTaskName()) == 1)
                            theSameTask = true;
                    }
                }

                if (similarTaskFound && !theSameTask) {
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
                } else if (!theSameTask) {
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

    // shared list of "all" tasks
    private ObservableList<ThisDayTasks> observableTodayTaskList;

    public void updateListView(int taskSelected, String currentTaskAssigned) {
        observableTodayTaskList = FXCollections.observableArrayList(
                PostgreSQLJDBC.getInstance()
                        .getSortedTaskByLengthInTimeRange(0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000),
                                0,
                                null
                        )
        );

        FilteredList<ThisDayTasks> filteredTasks = new FilteredList<>(observableTodayTaskList);
        tasksTable.setItems(filteredTasks);

        filteredTasks.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            String text = filterTasksTextField.getText();
            if (text == null || text.isEmpty())
                return null;
            else {
                final String lowerCase = text.toLowerCase();
                return (tasks) -> tasks.getTaskName().toLowerCase().contains(lowerCase);
            }
        }, filterTasksTextField.textProperty()));

        tasksTable.getSelectionModel().select(taskSelected);
        int positionInt = 0;
        int positionLeft = tasksTable.getItems().size() - 1;
        while (positionLeft >= 0) {

            if (currentTaskAssigned != null) {
                if (tasksTable.getSelectionModel().getSelectedItem().getTaskName().equals(currentTaskAssigned)) {
                    tasksTable.getSelectionModel().select(taskSelected - positionInt);
                    break;
                } else {
                    positionInt = positionInt + 1;
                    positionLeft = positionLeft - 1;
                    tasksTable.getSelectionModel().select(taskSelected - positionInt);
                }
            } else
                break;
        }
    }

    public void updateTodayTableView() {

        ObservableList<ThisDayTasks> filteredObservableTaskList = FXCollections.observableArrayList(observableTodayTaskList
                .stream()
                .peek(ThisDayTasks::setGoalDoneToday)
                .filter(x -> (x.getTaskDoneThisPeriod() > 1))
                .sorted((x, y) -> x.getTaskDoneThisPeriod() > y.getTaskDoneThisPeriod() ? -1 : 1)
                .collect(Collectors.toList())
        );

        // set the table not selectable
        todayTableView.setMouseTransparent(true);
        todayTableView.setFocusTraversable(false);
        todayTableView.setItems(filteredObservableTaskList);
    }

    public void updateWeeklyTableView() {

        // offset by 6 day, where 7th day is today (week = 6 past days + today)
        ObservableList<ThisWeekTasks> observableWeekTaskList = FXCollections.observableArrayList(
                PostgreSQLJDBC.getInstance()
                        .getSortedTaskByLengthInTimeRange(0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000),
                                6,
                                null
                        )
        );

        ObservableList<ThisWeekTasks> filteredObservableTaskList = FXCollections.observableArrayList(observableWeekTaskList
                .stream()
                .peek(ThisWeekTasks::setGoalDoneThisWeek)
                .filter(x -> (x.getTaskDoneThisPeriod() > 1))
                .sorted((x, y) -> x.getTaskDoneThisPeriod() > y.getTaskDoneThisPeriod() ? -1 : 1)
                .collect(Collectors.toList())
        );

        // set the table not selectable
        weekTableView.setMouseTransparent(true);
        weekTableView.setFocusTraversable(false);
        weekTableView.setItems(filteredObservableTaskList);
    }


    //======================================== Tab 2 MyAnalysis ========================================//

    public void showDataOnTab2Graphs() {

        List<Weekdays> coll = Arrays.stream(Weekdays.values()).collect(Collectors.toList());
        dayChoiceBox.setItems(FXCollections.observableArrayList(coll));
        dayChoiceBox.setValue(Weekdays.EVERYDAY);
        List<ThisDayTasks> tab2TasksChoiceList = new ArrayList<>(observableTodayTaskList);
        ThisDayTasks allTasks = new ThisDayTasks(ThisDayTasks.TodayTasksChoice.ALLTASKS.toString());
        tab2TasksChoiceList.add(0, allTasks);
        taskTab2ChoiceBox.setItems(FXCollections.observableArrayList(tab2TasksChoiceList));
        taskTab2ChoiceBox.setValue(allTasks);

        AtomicReference<Integer> taskChosen = new AtomicReference<>(Weekdays.EVERYDAY.nDay);
        AtomicReference<Integer> dayChosen = new AtomicReference<>(0);
        AtomicReference<LocalDate> startDate = new AtomicReference<>(LocalDate.of(1970, 1, 1));
        AtomicReference<LocalDate> endDate = new AtomicReference<>(LocalDate.now().plus(1, ChronoUnit.DAYS));

        taskTab2ChoiceBox.valueProperty().addListener((observableValue, thisDayTasks, t1) -> {
            if (thisDayTasks != t1) {
                System.out.println("taskTab2ChoiceBox value has changed " + observableValue.getValue().taskID.get());
                taskChosen.set(observableValue.getValue().taskID.get());
                System.out.println(taskChosen + " " + dayChosen + " " + startDate + " " + endDate);
                populateGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        dayChoiceBox.valueProperty().addListener((observableValue, todayTasks, t1) -> {
            if (todayTasks != t1) {
                System.out.println("dayChoiceBox value has changed " + observableValue.getValue().nDay);
                dayChosen.set(observableValue.getValue().nDay);
                System.out.println(taskChosen + " " + dayChosen + " " + startDate + " " + endDate);
                populateGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                System.out.println("startDatePicker value has changed " + observableValue.getValue());
                startDate.set(observableValue.getValue());
                System.out.println(taskChosen + " " + dayChosen + " " + startDate + " " + endDate);
                populateGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                System.out.println("endDatePicker value has changed " + observableValue.getValue());
                endDate.set(observableValue.getValue());
                System.out.println(taskChosen + " " + dayChosen + " " + startDate + " " + endDate);
                populateGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

    }

    // Tab 2
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private ChoiceBox<ThisDayTasks> taskTab2ChoiceBox;
    @FXML
    private ChoiceBox<Weekdays> dayChoiceBox;
    @FXML
    private ScatterChart<LocalDateTime, Long> taskScatterChart;
    @FXML
    private BarChart taskBarChart;


//    @FXML
//    private xAxis xAxisScatterChart;


    // https://stackoverflow.com/questions/12478758/converting-a-jfreechart-timeseries-series-with-day-data-to-week-or-month-data/12481509#12481509
    // https://stackoverflow.com/questions/13064406/javafx-real-time-linechart-with-time-axis
    // https://stackoverflow.com/questions/25892695/tooltip-on-line-chart-showing-date

    public void populateGraph(int taskId, LocalDate startTime, LocalDate endTime, int dayChosen) {
        List<ThisWeekTasks> tasksInTimeRangeMappedToDays =
                PostgreSQLJDBC.getInstance()
                        .getSortedTaskByLengthInTimeRange(taskId,
                                Timestamp.valueOf(startTime.atStartOfDay()),
                                Timestamp.valueOf(endTime.atStartOfDay()),
                                0,
                                Weekdays.getEnumByNDay(dayChosen));

        for (ThisWeekTasks task : tasksInTimeRangeMappedToDays) {
            LinkedHashMap<LocalDate, Long> collected =
                    task.getHowLongTaskDoneADay()
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            task.setHowLongTaskDoneADay(collected);
        }

//        XYChart.Series<String, String> series = new XYChart.Series<>();
//        tasksInTimeRangeMappedToDays.forEach(x ->
//                x.getHowLongTaskDoneADay().forEach(
//                        (k,v) -> series.getData()
//                            .addAll(new XYChart.Data<>( k.toString(),
//                                        String.format("%02d:%02d",
//                                        TimeUnit.MILLISECONDS.toHours(v),
//                                        TimeUnit.MILLISECONDS.toMinutes(v) -
//                                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(v))  ))) ));


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");


        XYChart.Series<LocalDateTime, Long> series = new XYChart.Series<>();
        tasksInTimeRangeMappedToDays.forEach(x ->
                x.getHowLongTaskDoneADay()
                        .forEach((k,v) ->
                                series.getData().addAll( new XYChart.Data<>( LocalDateTime.of(k.getYear(), k.getMonthValue(), k.getDayOfMonth(), 0 ,0, 0) , v ) )  // java.sql.Date.valueOf(k) k.atStartOfDay().toInstant(ZoneOffset.UTC).getEpochSecond()
                        )
        );

        ObservableList<XYChart.Series<LocalDateTime, Long>> filteredObservableTaskList = FXCollections.observableArrayList( series );

        taskScatterChart.setData(filteredObservableTaskList);
        taskScatterChart.setAnimated(false);

    }
}

