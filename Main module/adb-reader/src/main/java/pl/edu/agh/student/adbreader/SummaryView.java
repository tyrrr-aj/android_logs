package pl.edu.agh.student.adbreader;

import com.google.gson.Gson;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import pl.edu.agh.student.adbreader.requests.Request;
import pl.edu.agh.student.adbreader.requests.Response;
import pl.edu.agh.student.adbreader.utils.DataCollector;
import pl.edu.agh.student.adbreader.utils.Log;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.edu.agh.student.adbreader.utils.Utils.sendLogsInPack;

public class SummaryView {
    SummaryView() {
    }

    public void showSummary(DataCollector dataCollector) {
        VBox vBox = new VBox(createLineChart(dataCollector));

        Stage s = new Stage();
        s.setTitle("Summary");
        s.setScene(new Scene(vBox, 1000, 600));
        s.show();
    }

    private HashMap<Integer, XYChart.Series> prepareData(DataCollector dataCollector) {
        Map.Entry<Long, List<Log>> minEntry = dataCollector.getLogs().entrySet().stream()
            .min(Comparator.comparingLong(Map.Entry::getKey)).orElse(null);
        long minTime = (minEntry == null)? 0 : minEntry.getKey();

        HashMap<Integer, XYChart.Series> result = new HashMap<>();
        for(Map.Entry<Long, List<Log>> entry : dataCollector.getLogs().entrySet()) {
            long time = entry.getKey();
            List<Log> value = entry.getValue();

            Map<Integer, List<Log>> maps = value.stream().collect(Collectors.groupingBy(Log::getPid));
            for(Map.Entry<Integer, List<Log>> e : maps.entrySet()) {
                int pid = e.getKey();
                List<Log> logs = e.getValue();
                List<String> lines = logs.stream().map(Log::getLine).collect(Collectors.toList());

                Response response = sendLogsInPack(new Request(pid, lines));
                XYChart.Series dataSeries = result.getOrDefault(pid, new XYChart.Series());
                dataSeries.setName(String.valueOf(pid));
                dataSeries.getData().add(new XYChart.Data((time-minTime)*10, response.getScore()));
                result.put(pid, dataSeries);
            }
        }

        return result;
    }

    private LineChart createLineChart(DataCollector dataCollector) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Seconds from start reading logs");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(1);
        yAxis.setTickUnit(0.1);
        yAxis.setLabel("Risk");

        LineChart lineChart = new LineChart(xAxis, yAxis);

        HashMap<Integer, XYChart.Series> data = prepareData(dataCollector);
        for(Map.Entry<Integer, XYChart.Series> entry : data.entrySet()) {
            lineChart.getData().add(entry.getValue());
        }

        return lineChart;
    }
}
