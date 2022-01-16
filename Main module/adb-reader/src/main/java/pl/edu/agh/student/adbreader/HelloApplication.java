package pl.edu.agh.student.adbreader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.android.ddmlib.*;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import pl.edu.agh.student.adbreader.utils.DataCollector;

import static pl.edu.agh.student.adbreader.utils.Const.ADB;

public class HelloApplication extends Application
{
    private static Process processLogger;
    private static DataCollector dataCollector;

    @Override
    public void start(Stage stage)
    {
        dataCollector = new DataCollector();

        HBox hbox = createHBox();
        Text[] onlyDisplayText = {new Text("."), new Text("."), new Text("."), new Text(":5555")};
        TextField[] addressIp = {new TextField(""), new TextField(""), new TextField(""), new TextField("")};
        for (int i=0; i<addressIp.length; i++) {
            addressIp[i].setPrefWidth(40);
            hbox.getChildren().addAll(addressIp[i], onlyDisplayText[i]);
        }

        Button connectButton = new Button("Connect WiFi");
        Button disconnectButton = new Button("Disconnect");
        disconnectButton.setDisable(true);
        hbox.getChildren().addAll(connectButton, disconnectButton);

        TextArea textArea = new TextArea("");
        textArea.setPrefHeight(540);
        textArea.setPrefWidth(750);

        TableView tableView = new TableView();
        tableView.setPrefWidth(250);

        TableColumn<pl.edu.agh.student.adbreader.utils.TableRow, String> column1 = new TableColumn<>("PID");
        column1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<pl.edu.agh.student.adbreader.utils.TableRow, String> column2 = new TableColumn<>("Risk");
        column2.setCellValueFactory(new PropertyValueFactory<>("result"));

        tableView.getColumns().add(column1);
        tableView.getColumns().add(column2);


        HBox bottomHBox = new HBox(textArea, tableView);

        VBox vbox = new VBox(hbox, bottomHBox);

        AtomicReference<Boolean> isConnected = new AtomicReference<>(true);

        AndroidDebugBridge.init(false);
        AndroidDebugBridge debugBridge = AndroidDebugBridge.createBridge(ADB, true);
        if (debugBridge == null) {
            System.err.println("Invalid ADB location.");
            System.exit(1);
        }

        AndroidDebugBridge.addDeviceChangeListener(new IDeviceChangeListener() {
            @Override
            public void deviceChanged(IDevice device, int arg1) {
                // not implemented
            }

            @Override
            public void deviceConnected(IDevice device) {
                Platform.runLater(() -> {
                    textArea.appendText("Device (" + device.getSerialNumber() + ") connected with USB\n");
                    for (TextField addressIpField : addressIp)
                    {
                        addressIpField.setDisable(true);
                    }
                    connectButton.setText("Connect USB");
                });
            }

            @Override
            public void deviceDisconnected(IDevice device) {
                Platform.runLater(() -> {
                    textArea.appendText("Device (" + device.getSerialNumber() + ") disconnected with USB\n");

                    for (TextField addressIpField : addressIp)
                    {
                        addressIpField.setDisable(false);
                    }
                    connectButton.setText("Connect WiFi");
                });
            }
        });

        disconnectButton.setOnAction(event -> {
            disconnectButton.setDisable(true);
            dataCollector.stopThread();
            textArea.appendText("Device successfully disconnected!\nPlease wait for summary modal!");

            if (processLogger != null && processLogger.isAlive()) {
                processLogger.destroy();
            }

            SummaryView summaryView = new SummaryView();
            summaryView.showSummary(dataCollector);

            if (connectButton.getText().equals("Connect WiFi")) {
                for (TextField addressIpField : addressIp) {
                    addressIpField.setDisable(false);
                }
            }

            connectButton.setDisable(false);
            dataCollector.getLogs().clear();
        });

        connectButton.setOnAction(event -> {
            Platform.runLater(() -> {
                connectButton.setDisable(true);
            });

            if (connectButton.getText().equals("Connect WiFi"))
            {
                for (TextField addressIpField : addressIp)
                {
                    addressIpField.setDisable(true);
                }
                textArea.appendText("Trying to connect...\n");

                String deviceIp = Arrays.stream(addressIp).map(TextField::getText).collect(Collectors.joining(","));

                runTcpIpThread();
                runWiFiThread(textArea, tableView, deviceIp, new Date().getTime()).start();

                if (isConnected.get())
                {
                    Platform.runLater(() -> {
                        textArea.appendText("Successfully connected!\n");
                        disconnectButton.setDisable(false);
                    });

                    runUsbThread(textArea, tableView, new Date().getTime()).start();
                }
                else
                {
                    Platform.runLater(() -> {
                        textArea.appendText("Cannot connect to provided device\n");
                        for (TextField addressIpField : addressIp)
                        {
                            addressIpField.setDisable(false);
                        }
                        connectButton.setDisable(false);
                    });
                }
            } else {
                Platform.runLater(() -> {
                    disconnectButton.setDisable(false);
                });

                runUsbThread(textArea, tableView, new Date().getTime()).start();
            }
        });

        Scene scene = new Scene(vbox, 1000, 600);
        stage.setTitle("Malware analysis");
        stage.setScene(scene);
        stage.show();
    }

    public static HBox createHBox() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(20));
        hbox.setSpacing(5);
        hbox.setAlignment(Pos.CENTER);

        return hbox;
    }

    public static void runTcpIpThread() {
        List<String> command = new ArrayList<>();
        command.add(ADB);
        command.add("tcpip");
        command.add("5555");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        try
        {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ( (line = reader.readLine()) != null) {

            }
            process.destroy();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Thread runWiFiThread(TextArea textArea, TableView tableView, String deviceIp, long startTime) {
        return new Thread(() -> {
            List<String> command = new ArrayList<>();
            command.add(ADB);
            command.add("connect");
            command.add(deviceIp + ":5555");

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            try
            {
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(HelloApplication.processLogger.getInputStream()));
                StringBuilder sb = new StringBuilder();

                int i=0;
                String line;
                while ((line = reader.readLine()) != null)
                {
                    dataCollector.addLog(line, startTime, tableView);
                    sb.append(line).append("\n");
                    if (i > 10)
                    {
                        Platform.runLater(() -> {
                            textArea.appendText(sb.toString());
                            sb.setLength(0);
                        });
                        i = 0;
                    }
                    i++;
                }
                process.destroy();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    public static Thread runUsbThread(TextArea textArea, TableView tableView, long startTime) {
        dataCollector.setBaseTime(startTime/10000);

        return new Thread(() -> {
            List<String> command = new ArrayList<>();
            command.add(ADB);
            command.add("logcat");
            ProcessBuilder processLogger = new ProcessBuilder(command);

            try
            {
                HelloApplication.processLogger = processLogger.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(HelloApplication.processLogger.getInputStream()));
                StringBuilder sb = new StringBuilder();

                int i=0;
                String line;
                while ( (line = reader.readLine()) != null) {
                    dataCollector.addLog(line, startTime, tableView);
                    sb.append(line).append("\n");
                    if (i > 10)
                    {
                        Platform.runLater(() -> {
                            textArea.appendText(sb.toString());
                            sb.setLength(0);
                        });
                        i = 0;
                    }
                    i++;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args)
    {
        launch();
    }
}
