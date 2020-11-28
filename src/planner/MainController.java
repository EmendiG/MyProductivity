package planner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.util.StringConverter;


public class MainController {

    @FXML
    private VBox mainWindow;


    // Tab 1
    @FXML
    private ListView<ThisWeekTasks> tasksTable;
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
        // TAB 2
        setVariablesForTab2Graph();
        populateLinearGraph(0, LocalDate.of(1970,1,1), LocalDate.now().plusDays(1), 0);
        populateBarChart();
        // TAB 3
        setVariablesForTab3Table();
        populateMonthTable(LocalDate.of(1970,1,1), LocalDate.now().plusDays(1));
        populateAllTaskTable();
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
            currentlyAssignedTask = Postgresql.getInstance().insertTask(selectedTask);
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
            Postgresql.getInstance().insertEndTime(currentlyAssignedTask.getID());
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
    private void showCurrentTimeElapsed() {
        taskTimer.startTimer(currentTimeLabel);
    }

    @FXML
    private void stopCurrentTimeElapsed() {
        taskTimer.stopTimer();
        currentTimeLabel.setText("None");
    }

    @FXML
    private void showNewTaskWindow() {
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
                List<String> listedTasknames = Postgresql.getInstance().getListedTasknames();
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
                        Postgresql.getInstance().insertNewGoal(newTask);
                        successfullyAddedTaskAlert.show();
                        updateListView(tasksTable.getItems().size(), newTask.getNewTaskName());
                    }
                } else if (!theSameTask) {
                    Postgresql.getInstance().insertNewGoal(newTask);
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
    private ObservableList<ThisWeekTasks> observableAllTaskList;

    private void updateListView(int taskSelected, String currentTaskAssigned) {
        observableAllTaskList = FXCollections.observableArrayList(
                Postgresql.getInstance()
                        .getSortedTaskByLengthInTimeRange(0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000),
                                0,
                                null
                        )
        );

        FilteredList<ThisWeekTasks> filteredTasks = new FilteredList<>(observableAllTaskList);
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

    private void updateTodayTableView() {

        ObservableList<ThisDayTasks> filteredObservableTaskList = FXCollections.observableArrayList(observableAllTaskList
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

    private void updateWeeklyTableView() {

        // offset by 6 day, where 7th day is today (week = 6 past days + today)
        ObservableList<ThisWeekTasks> observableWeekTaskList = FXCollections.observableArrayList(
                Postgresql.getInstance()
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

    @FXML
    private DatePicker startDatePickerTab2;
    @FXML
    private DatePicker endDatePickerTab2;
    @FXML
    private ChoiceBox<ThisDayTasks> taskChoiceBoxTab2;
    @FXML
    private ChoiceBox<Weekdays> dayChoiceBox;

    @FXML
    private LineChart<LocalDateTime, Long> taskLinearChart;
    @FXML
    private NumberAxis yAxisLinearChart;
    @FXML
    private DateAxis310 xAxisLinearChart;

    @FXML
    private AnchorPane anchorPane;

    private void setVariablesForTab2Graph() {

        List<Weekdays> coll = Arrays.stream(Weekdays.values()).collect(Collectors.toList());
        dayChoiceBox.setItems(FXCollections.observableArrayList(coll));
        dayChoiceBox.setValue(Weekdays.EVERYDAY);
        // filtered List shows tasks by LocalDate occurrences, so that in graph there would be at least 2 points on each measurement
        List<ThisDayTasks> tab2TasksChoiceList = observableAllTaskList.stream().filter(x -> x.getHowLongTaskDoneADay().size() > 1).collect(Collectors.toList());
        // add ALL TASKS field to the list
        ThisDayTasks allTasks = new ThisDayTasks(ThisDayTasks.TodayTasksChoice.ALLTASKS.toString());
        tab2TasksChoiceList.add(0, allTasks);
        taskChoiceBoxTab2.setItems(FXCollections.observableArrayList(tab2TasksChoiceList));
        taskChoiceBoxTab2.setValue(allTasks);

        AtomicReference<Integer> taskChosen = new AtomicReference<>(0);
        AtomicReference<Integer> dayChosen = new AtomicReference<>(Weekdays.EVERYDAY.nDay);
        AtomicReference<LocalDate> startDate = new AtomicReference<>(LocalDate.of(1970, 1, 1));
        AtomicReference<LocalDate> endDate = new AtomicReference<>(LocalDate.now().plus(1, ChronoUnit.DAYS));

        taskChoiceBoxTab2.valueProperty().addListener((observableValue, thisDayTasks, t1) -> {
            if (thisDayTasks != t1) {
                taskChosen.set(observableValue.getValue().taskID.get());
                populateLinearGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        dayChoiceBox.valueProperty().addListener((observableValue, todayTasks, t1) -> {
            if (todayTasks != t1) {
                dayChosen.set(observableValue.getValue().nDay);
                populateLinearGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        startDatePickerTab2.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                startDate.set(observableValue.getValue());
                populateLinearGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

        endDatePickerTab2.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                endDate.set(observableValue.getValue());
                populateLinearGraph(taskChosen.get(), startDate.get(), endDate.get(), dayChosen.get());
            }
        });

    }

    private void populateLinearGraph(int taskId, LocalDate startTime, LocalDate endTime, int dayChosen) {
        List<AllTimeTasks> tasksInTimeRangeMappedToDays =
                Postgresql.getInstance()
                        .getSortedTaskByLengthInTimeRange(taskId,
                                Timestamp.valueOf(startTime.atStartOfDay()),
                                Timestamp.valueOf(endTime.atStartOfDay()),
                                0,
                                Weekdays.getEnumByNDay(dayChosen)
                        );

        // sort task's howLongTaskDoneADay by a KEY<LocalDate>
        for (ThisWeekTasks task : tasksInTimeRangeMappedToDays) {
            LinkedHashMap<LocalDate, Long> collected =
                    task.getHowLongTaskDoneADay()
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
            task.setHowLongTaskDoneADay(collected);
        }

        // "series" line is transparent <taskLinearChart.css> only mark points are visible
        XYChart.Series<LocalDateTime, Long> series = new XYChart.Series<>();
        XYChart.Series<LocalDateTime, Long> regression = new XYChart.Series<>();
        List<Double> doubleXList = new ArrayList<>();
        List<Double> doubleYList = new ArrayList<>();

        if (tasksInTimeRangeMappedToDays.size() == 1) {
            tasksInTimeRangeMappedToDays.forEach(x ->
                    x.getHowLongTaskDoneADay()
                            .forEach((k, v) -> {
                                series.getData().addAll(new XYChart.Data<>(LocalDateTime.of(k.getYear(), k.getMonthValue(), k.getDayOfMonth(), 0, 0, 0), v));
                                doubleXList.add((double) k.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                                doubleYList.add(v.doubleValue());
                            })
            );
            series.setName( tasksInTimeRangeMappedToDays.get(0).getTaskName().trim() );
            regression.setName("regression");

        } else {
            // "ALL TASKS" should be summed up to one value
            Map<LocalDate, Long> data = new HashMap<>();
            for (ThisWeekTasks task : tasksInTimeRangeMappedToDays) {
                for (LocalDate key : task.getHowLongTaskDoneADay().keySet()) {
                    if (data.containsKey(key))
                        data.put(key, data.get(key) + task.getHowLongTaskDoneADay().get(key));
                    else
                        data.put(key, task.getHowLongTaskDoneADay().get(key));
                }
            }
            data.forEach((k,v) ->  {
                series.getData().addAll( new XYChart.Data<>( LocalDateTime.of(k.getYear(), k.getMonthValue(), k.getDayOfMonth(), 0 ,0, 0) , v ) );
                doubleXList.add((double) k.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli());
                doubleYList.add(v.doubleValue());
            });
            series.setName(ThisDayTasks.TodayTasksChoice.ALLTASKS.toString());
            regression.setName("regression");
        }

        double[] doubleXArray = doubleXList.stream().mapToDouble(Double::doubleValue).toArray();
        double[] doubleYArray = doubleYList.stream().mapToDouble(Double::doubleValue).toArray();
        LinearRegression linearRegression = new LinearRegression(doubleXArray, doubleYArray);

        // there are always some values because there is a check if there are at least 2 LocalDate values in the task list
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        LocalDateTime firstDate = series.getData().stream().min(Comparator.comparing(XYChart.Data::getXValue)).get().getXValue();
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        LocalDateTime lastDate = series.getData().stream().max(Comparator.comparing(XYChart.Data::getXValue)).get().getXValue();
        long firstXValue = firstDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        long lastXValue = lastDate.toInstant(ZoneOffset.UTC).toEpochMilli();
        long firstPrediction = (long) linearRegression.predict(firstXValue);
        long lastPrediction =  (long) linearRegression.predict(lastXValue);

        regression.getData().addAll(new XYChart.Data<>(LocalDateTime.of(firstDate.getYear(), firstDate.getMonthValue(), firstDate.getDayOfMonth(), 0, 0, 0), firstPrediction) );
        regression.getData().addAll(new XYChart.Data<>(LocalDateTime.of(lastDate.getYear(), lastDate.getMonthValue(), lastDate.getDayOfMonth(), 0, 0, 0), lastPrediction) );

        xAxisLinearChart.setTickLabelRotation(-15);
        yAxisLinearChart.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(number.longValue()),
                        TimeUnit.MILLISECONDS.toMinutes(number.longValue()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(number.longValue())));
            }
            @Override
            public Number fromString(String s) {
                return 0;
            }
        });

        if (!taskLinearChart.getData().isEmpty())
            taskLinearChart.getData().clear();
        taskLinearChart.getData().addAll(series, regression);

        taskLinearChart.setAnimated(false);
        taskLinearChart.getData().forEach(x -> {
            if (x.getName().equals(series.getName()))
                x.getNode().toFront();
            else
                x.getNode().toBack();
        });
    }

    private void populateBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        CustomBar<String, Long> taskBarChart = new CustomBar<>(xAxis, yAxis );
        XYChart.Series<String, Long> series = new XYChart.Series<>();

        observableAllTaskList.forEach(x ->
                series.getData().addAll( new XYChart.Data<>(x.getTaskName(), x.getTaskDuration()) )
        );

        yAxis.setTickLabelFormatter(new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                return String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(number.longValue()),
                        TimeUnit.MILLISECONDS.toMinutes(number.longValue()) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(number.longValue())));
            }
            @Override
            public Number fromString(String s) {
                return 0;
            }
        });

        anchorPane.getChildren().add(0, taskBarChart);
        taskBarChart.setLayoutY(346);
        taskBarChart.setPrefHeight(280);
        taskBarChart.setPrefWidth(845);
        taskBarChart.getStylesheets().add("taskCustomBar.css");

        taskBarChart.setData(FXCollections.observableArrayList(series));
    }

    //======================================== Tab 3 MyData ========================================//

    @FXML
    private DatePicker startDatePickerTab3;
    @FXML
    private DatePicker endDatePickerTab3;
    @FXML
    private TableView<ThisMonthTasks> monthTableView;
    @FXML
    private  TableView<AllTimeTasks> allTasksTableView;

    private void setVariablesForTab3Table() {

        AtomicReference<LocalDate> startDate = new AtomicReference<>(LocalDate.of(1970, 1, 1));
        AtomicReference<LocalDate> endDate = new AtomicReference<>(LocalDate.now().plus(1, ChronoUnit.DAYS));

        startDatePickerTab3.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                startDate.set(observableValue.getValue());
                populateMonthTable(startDate.get(), endDate.get());
            }
        });

        endDatePickerTab3.valueProperty().addListener((observableValue, localDate, t1) -> {
            if (localDate != t1) {
                endDate.set(observableValue.getValue());
                populateMonthTable(startDate.get(), endDate.get());
            }
        });
    }

    private void populateMonthTable(LocalDate startTime, LocalDate endTime) {
        List<AllTimeTasks> tasksInTimeRangeMappedToDays =
                Postgresql.getInstance()
                        .getSortedTaskByLengthInTimeRange(0,
                                Timestamp.valueOf(startTime.atStartOfDay()),
                                Timestamp.valueOf(endTime.atStartOfDay()),
                                30,
                                Weekdays.EVERYDAY.weekday
                        );

        ObservableList<ThisMonthTasks> filteredObservableTaskList = FXCollections.observableArrayList(tasksInTimeRangeMappedToDays
                .stream()
                .peek(ThisMonthTasks::setGoalDoneThisMonth)
                .peek(ThisMonthTasks::setAverageTaskDoneThisMonth)
                .sorted((x, y) -> x.getTaskDoneThisPeriod() > y.getTaskDoneThisPeriod() ? -1 : 1)
                .collect(Collectors.toList())
        );

        monthTableView.setItems(filteredObservableTaskList);
    }

    private void populateAllTaskTable() {
        ObservableList<AllTimeTasks> observableAllTaskAllTimeList = FXCollections.observableArrayList(
                Postgresql.getInstance()
                        .getSortedTaskByLengthInTimeRange(0,
                                new Timestamp(0L),
                                new Timestamp(System.currentTimeMillis() + 10000),
                                999999,
                                null
                        )
        );
        observableAllTaskAllTimeList.stream()
                .peek( x -> x.setGoalDateInString(x.getGoalDate()))
                .peek( x -> x.setGoalDurationInString(x.getGoalDuration()))
                .peek(AllTimeTasks::setDaysTaskPerformed)
                .forEach(AllTimeTasks::setAverageTaskDoneAllTime);

        allTasksTableView.setItems(observableAllTaskAllTimeList);
    }


}

