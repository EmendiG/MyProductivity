package planner;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class NewTaskController {

    @FXML
    private DialogPane newTaskWindow;

    @FXML
    private ChoiceBox<String> goalChoiceBox;
    @FXML
    private TextField nameTextField;
    @FXML
    private Label dateLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label hourLabel;
    @FXML
    private Spinner<Double> hourSpinner;



    @FXML
    public void initialize() {
        String st[] = { "None", "Daily", "Weekly", "Monthly", "Till date" };
        try {
            goalChoiceBox.setItems(FXCollections.observableArrayList(st));
            goalChoiceBox.setValue("None");
            SpinnerValueFactory<Double> spinnerValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 9999D, 0, 0.5);
            hourSpinner.setValueFactory(spinnerValueFactory);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void showAdditionalElements() {
        boolean visible;
        if (goalChoiceBox.getValue() != "None") {
            visible = true;
        } else {
            visible = false;
            datePicker.setValue(null);
            if (hourSpinner.getValueFactory() != null)
                hourSpinner.getValueFactory().setValue(null);
        }
        dateLabel.setVisible(visible);
        hourLabel.setVisible(visible);
        datePicker.setVisible(visible);
        hourSpinner.setVisible(visible);
    }

    public NewTasks processResults() {
        String newTaskName = nameTextField.getText().trim();
        String goalChoice = goalChoiceBox.getValue();
        Double goalHours = hourSpinner.getValue();
        LocalDate pickedDate = datePicker.getValue();

        if ( (!goalChoice.equals("None") && (goalHours == null || goalHours == 0 )) ||
             (goalChoice.equals("Till date") && pickedDate == null )                ||
             (goalChoice.equals("None") && nameTextField.getText().isEmpty() )      ||
             (nameTextField.getText().isEmpty()) ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Wrong variables:");
            if (nameTextField.getText().isEmpty())
                alert.setContentText("Task name cannot be empty");
            else if (goalHours == null || goalHours == 0 )
                alert.setContentText("Hours set cannot be empty");
            else if (goalChoice.equals("Till date"))
                alert.setContentText("Pick a date!");
            alert.showAndWait();
            return new NewTasks();

        } else {
//            System.out.println(newTaskName + " " + goalChoice + " " + goalHours + " " + pickedDate);
            return new  NewTasks(newTaskName, goalChoice, goalHours, pickedDate);
        }
    }
}
