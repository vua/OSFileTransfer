package com.cooooode.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    private ArrayList<String> paths=new ArrayList<>();
    private ExecutorService executorService=Executors.newCachedThreadPool();
    public void start(final Stage primaryStage) throws Exception {
        Properties properties = new Properties();
        InputStream in = App.class.getClassLoader().getResourceAsStream("config.properties");
        properties.load(in);
        primaryStage.setTitle("OSFileTransfer (Version 1.0)");
        primaryStage.setResizable(false);
        primaryStage.setWidth(300);

        VBox top_vbox = new VBox();
        Label label_host= new Label("Host");
        Label label_user= new Label("User");
        Label label_port=new Label("Port");
        Label label_password= new Label("Password");
        label_host.setStyle(
                "-fx-pref-width: 60;" +
                "-fx-text-fill: #fff");
        label_user.setStyle(
                "-fx-pref-width: 60;" +
                "-fx-text-fill: #fff");
        label_port.setStyle(
                "-fx-pref-width: 60;" +
                        "-fx-text-fill: #fff");
        label_password.setStyle(
                "-fx-pref-width: 60;" +
                "-fx-text-fill: #fff");
        TextField textField_host=new TextField();
        TextField textField_user=new TextField();
        TextField textField_port=new TextField();
        TextField textField_password=new TextField();
        textField_host.setStyle("-fx-pref-width: 200;");
        textField_user.setStyle("-fx-pref-width: 200;");
        textField_port.setStyle("-fx-pref-width: 200;");
        textField_password.setStyle("-fx-pref-width: 200;");
        textField_host.setText(properties.getProperty("host"));
        textField_user.setText(properties.getProperty("user"));
        textField_port.setText(properties.getProperty("port"));
        textField_password.setText(properties.getProperty("password"));
        HBox hBox_host=new HBox();
        hBox_host.getChildren().addAll(label_host,textField_host);
        HBox hBox_user=new HBox();
        hBox_user.getChildren().addAll(label_user,textField_user);
        HBox hBox_port=new HBox();
        hBox_port.getChildren().addAll(label_port,textField_port);
        HBox hBox_password=new HBox();
        hBox_password.getChildren().addAll(label_password,textField_password);
        top_vbox.getChildren().addAll(hBox_host,hBox_user,hBox_port,hBox_password);

        for (Node node:
             top_vbox.getChildren()) {
            node.setStyle("-fx-padding: 5 0;" );
        }

        top_vbox.setStyle(
                "-fx-background-color: #404040;"+
                "-fx-padding: 10 20 10 20;"

        );
        final VBox file_vbox=new VBox();

        ScrollPane filesp = new ScrollPane();
        filesp.setStyle(
                "-fx-background-color:#303030;" +
                "-fx-padding: 10 20 10 20;" +
                "-fx-pref-height: 300;");
        file_vbox.setStyle("-fx-background-color:#303030;");
        filesp.setOnDragOver((event)-> {
                Dragboard dragboard = event.getDragboard();
                if (dragboard.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                }
            });

        filesp.setOnDragDropped((event)-> {
                Dragboard dragboard = event.getDragboard();
                if (event.isAccepted()) {
                    List<File> files = dragboard.getFiles();

                    for (File file:
                         files) {
                        Label file_label=new Label(file.getName());
                        paths.add(file.getAbsolutePath());
                        if(file.isDirectory()){
                            file_label.setStyle(
                                    "-fx-text-fill: #fff;" +
                                    "-fx-background-color: #5c5c5c;" +
                                    "-fx-alignment: CENTER;" +
                                    "-fx-pref-width: 256;" +
                                    "-fx-padding: 2px;" +
                                    "-fx-text-fill: #1abc9c;" +
                                    "-fx-border-width: 0.2;" +
                                    "-fx-border-color: #fff");
                        }else{
                            file_label.setStyle(
                                    "-fx-text-fill: #fff;" +
                                    "-fx-background-color: #5c5c5c;" +
                                    "-fx-alignment: CENTER;" +
                                    "-fx-pref-width: 256;" +
                                    "-fx-padding: 2px;" +
                                    "-fx-text-fill: #fff;" +
                                    "-fx-border-width: 0.2;" +
                                    "-fx-border-color: #fff");
                        }
                        file_vbox.getChildren().add(file_label);

                    }

                }
        });

        filesp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        filesp.setContent(file_vbox);
        VBox buttom_vbox=new VBox();
        Button button_connection=new Button("Connect and Submit Files");

        button_connection.setOnMouseClicked((event)->{

            Stage message=new Stage();
            message.setTitle("INFORMATION");
            TextArea textArea_mess=new TextArea();
            textArea_mess.setStyle("-fx-text-fill: #0a1016");
            Scene scene=new Scene(textArea_mess);
            textArea_mess.setEditable(false);
            message.setScene(scene);
            message.show();

            String host=textField_host.getText();
            String user=textField_user.getText();
            String port=textField_port.getText();
            String password=textField_password.getText();
            StringBuilder log= new StringBuilder();
            if(host.equals("")||user.equals("")||password.equals("")) {

                textArea_mess.setText("Please fill in the values of host,user,and password");
            }else {
                if(paths==null||paths.size()==0) return;
                String url = user + "@" + host;
                CountDownLatch latch = new CountDownLatch(paths.size());
                for (String path:
                        paths) {

                    executorService.execute(()->{
                            try {
                                Process process = Runtime.getRuntime().exec(
                                        "cmd.exe /c pscp " +
                                                " -r " +
                                                "-pw " + password +
                                                " -P " + port +
                                                " "+path+
                                                " " + url +
                                                ":/root/"

                                );
                                BufferedReader bufferedReader = new BufferedReader(
                                        new InputStreamReader(process.getInputStream()));
                                BufferedReader errReader = new BufferedReader(
                                        new InputStreamReader(process.getErrorStream()));
                                String err, str;
                                while ((err = errReader.readLine()) != null) {
                                    log.append(err+"\n");
                                    textArea_mess.setText(log.toString());
                                }
                                while ((str = bufferedReader.readLine()) != null) {
                                    log.append(str+"\n");
                                    textArea_mess.setText(log.toString());
                                }
                                process.destroy();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }finally {
                                latch.countDown();
                            }
                        });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                paths=new ArrayList<>();
                file_vbox.getChildren().clear();

            }

        });
            button_connection.setStyle(
                "-fx-text-fill:#fff;" +
                        "-fx-background-color: #5c5c5c;" +
                        "-fx-pref-width: 260;" +
                        "-fx-border-color: #fff;" +
                        "-fx-border-width: 0.2;" +
                        "-fx-start-margin: 5;");
        buttom_vbox.getChildren().add(button_connection);
        buttom_vbox.setStyle(
                "-fx-background-color: #303030;"+
                        "-fx-padding: 10 20 10 20;"

        );
        VBox vbox=new VBox();

        vbox.getChildren().addAll(top_vbox,filesp,buttom_vbox);

        Scene scene= new Scene(vbox);

        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
