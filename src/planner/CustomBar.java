package planner;

import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CustomBar<X, Y> extends BarChart<X, Y> {

    Map<Node, TextFlow> nodeMap = new HashMap<>();

    public CustomBar(Axis xAxis, Axis yAxis) {
        super(xAxis, yAxis);
        this.setBarGap(0.0);
        this.getYAxis().setTickLabelsVisible(false);
        this.getYAxis().setTickMarkVisible(false);
        this.setLegendVisible(false);
        this.getYAxis().setOpacity(0);
    }

    @Override
    protected void seriesAdded(Series<X, Y> series, int seriesIndex) {

        super.seriesAdded(series, seriesIndex);

        for (int j = 0; j < series.getData().size(); j++) {

            Data<X, Y> item = series.getData().get(j);

            Text text = new Text(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours((long) item.getYValue()),
                    TimeUnit.MILLISECONDS.toMinutes((long) item.getYValue()) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours((long) item.getYValue()))));

            text.setStyle("-fx-font-size: 10pt;");

            TextFlow textFlow = new TextFlow(text);
            textFlow.setTextAlignment(TextAlignment.CENTER);

            nodeMap.put(item.getNode(), textFlow);
            this.getPlotChildren().add(textFlow);

        }

    }

    @Override
    protected void seriesRemoved(final Series<X, Y> series) {

        for (Node bar : nodeMap.keySet()) {

            Node text = nodeMap.get(bar);
            this.getPlotChildren().remove(text);

        }

        nodeMap.clear();

        super.seriesRemoved(series);
    }

    @Override
    protected void layoutPlotChildren() {

        super.layoutPlotChildren();

        for (Node bar : nodeMap.keySet()) {

            TextFlow textFlow = nodeMap.get(bar);

            if (bar.getBoundsInParent().getHeight() > 30) {
                ((Text) textFlow.getChildren().get(0)).setFill(Color.WHITE);
                textFlow.resize(bar.getBoundsInParent().getWidth(), 200);
                textFlow.relocate(bar.getBoundsInParent().getMinX(), bar.getBoundsInParent().getMinY() + 10);
            } else {
                ((Text) textFlow.getChildren().get(0)).setFill(Color.BLACK);
                textFlow.resize(bar.getBoundsInParent().getWidth(), 200);
                textFlow.relocate(bar.getBoundsInParent().getMinX(), bar.getBoundsInParent().getMinY() - 20);
            }
        }
    }
}
