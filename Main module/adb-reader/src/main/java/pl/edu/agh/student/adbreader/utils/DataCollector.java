package pl.edu.agh.student.adbreader.utils;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableView;
import pl.edu.agh.student.adbreader.requests.Request;
import pl.edu.agh.student.adbreader.requests.Response;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.edu.agh.student.adbreader.utils.Utils.sendLogsInPack;

public class DataCollector
{
    private final HashMap<Long, List<Log>> logs;
    public HashMap<Integer, Double> score;
    private long baseTime;
    private Thread thread;

    public DataCollector()
    {
        this.logs = new HashMap<>();
        this.score = new HashMap<>();
        this.baseTime = 0;
    }

    public void addLog(String line, long startTime, TableView tableView)
    {
        if (!line.matches("\\s*\\d{1,2}-\\d{1,2}.*")) {
            return;
        }

        try {
            Log log = Utils.parseLogLine(line);
            long time = log.getTime()/10000;

            if (time > startTime/10000) {
                List<Log> logsForTime = logs.getOrDefault(time, new LinkedList<>());
                logsForTime.add(log);
                logs.put(time, logsForTime);

                if (time > this.baseTime) {
                    long oldTime = this.baseTime;
                    this.baseTime = time;

                    if (thread != null && thread.isAlive()) {
                        thread.stop();

                        Platform.runLater(() -> {
                            tableView.getItems().clear();
                            for(Map.Entry<Integer, Double> e : score.entrySet()) {
                                tableView.getItems().add(new TableRow(e.getKey(), e.getValue()));
                            }
                            tableView.refresh();
                        });
                    }

                    thread = new Thread(() -> {
                        List<Log> threadLogs = logs.getOrDefault(oldTime, new LinkedList<>());
                        Map<Integer, List<Log>> maps = threadLogs.stream().collect(Collectors.groupingBy(Log::getPid));

                        for(Map.Entry<Integer, List<Log>> e : maps.entrySet()) {
                            int pid = e.getKey();
                            List<Log> logs = e.getValue();
                            List<String> lines = logs.stream().map(Log::getLine).collect(Collectors.toList());

                            Response response = sendLogsInPack(new Request(pid, lines));
                            score.put(pid, response.getScore());
                        }

                        Platform.runLater(() -> {
                            tableView.getItems().clear();
                            for(Map.Entry<Integer, Double> e : score.entrySet()) {
                                tableView.getItems().add(new TableRow(e.getKey(), e.getValue()));
                            }
                            tableView.refresh();
                        });
                    });

                    thread.start();
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public HashMap<Long, List<Log>> getLogs()
    {
        return logs;
    }

    public long getBaseTime()
    {
        return baseTime;
    }

    public void setBaseTime(long baseTime)
    {
        this.baseTime = baseTime;
    }

    public void stopThread() {
        if (thread != null && thread.isAlive()) {
            thread.stop();
        }
    }
}
