package planner;

import javafx.application.Platform;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class TaskTimer {
    public Timer timer;
    public TimerTask timerTask;

        public TaskTimer() {}

        public Timer startTimer(Label currentTimeLabel) {
            timer = new Timer();
            LocalTime endTime = LocalTime.now ();
            DecimalFormat decimalFormat = new DecimalFormat("00");

            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            java.time.Duration diff = java.time.Duration.between(endTime, LocalTime.now ());
                            String decimalSecond = decimalFormat.format(diff.toSecondsPart());
                            String decimalMinute = decimalFormat.format(diff.toMinutesPart());
                            String decimalHour = decimalFormat.format(diff.toHoursPart());
                            currentTimeLabel.setText(decimalHour + ":" + decimalMinute + ":" + decimalSecond);
                        }
                    });
                }
            };
            timer.scheduleAtFixedRate(timerTask, 0, 1000);
            return timer;
        }

        public void stopTimer() {
            timerTask.cancel();
            timer.cancel();
            timer.purge();
        }
    }
