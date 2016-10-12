/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificatecreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import static javafx.scene.control.OverrunStyle.LEADING_ELLIPSIS;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.scene.paint.Color.rgb;

/**
 *
 * @author Andy
 */

public class CertificateCreator extends Application {
    
    String selectedStudent;
    
    @Override
    public void start(Stage primaryStage) {
        //get current date
        String date = getDate();
        //get user directory and config file directory
        String userPath = System.getProperty("user.dir");
        String configPath = userPath + File.separator + "config.txt";
        System.out.println("config path:\n" + configPath);
        //Perform initial check for student list
        String stdntLstPath = getStudentList(configPath);
        StringProperty stdntLstTxt = new SimpleStringProperty();
        List<String> studentList = new ArrayList<>();
        Label statusText = createStatusText(stdntLstTxt);
        Label fieldInstructions = new Label("Type name of desired student in " +
                "textbox. The student list will filter as you type.");
        Label listInstructions = new Label("Click name of desired student in " +
                "list to create certificate.");
        System.out.println("Directory path:\n" + stdntLstPath);
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected!");
            statusText.setTextFill(rgb(255, 0, 0));
        }
        else {
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            extractStudentList(studentList, stdntLstPath);
        }
        Label stdntLbl = new Label("Student Name:");
        TextField stdntFld = new TextField();
        ListView visibleStudentList = createVisibleStudentList(studentList,
                stdntFld);
        Button setDirBtn = createDirectoryButton(configPath, primaryStage);
        Scene scene = new Scene(createLayout(stdntLbl, stdntFld,
                visibleStudentList, setDirBtn, statusText, fieldInstructions,
                listInstructions));
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Writer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }

    private ListView createVisibleStudentList(List<String> studentList,
            TextField stdntFld) {
        ObservableList boxList = FXCollections.observableList(studentList);
        ListView<String> visibleStudentList = new ListView<>();
        visibleStudentList.setEditable(false);
        FilteredList<String> filteredItems = new FilteredList<>(boxList,
                p -> true);
        stdntFld.textProperty().addListener((obs, oldVal, newVal) -> {
            final String selected = visibleStudentList.getSelectionModel()
                    .getSelectedItem();
            Platform.runLater(() -> {
                if (selected == null || !selected.equals(stdntFld.getText())) {
                    filteredItems.setPredicate(boxListItem -> {
                        return boxListItem.toUpperCase().startsWith(newVal.
                                toUpperCase());
                    });
                }
            });
        });
        visibleStudentList.setItems(filteredItems);
        visibleStudentList.setOnMouseClicked((MouseEvent event) -> {
            selectedStudent = visibleStudentList.getSelectionModel()
                    .getSelectedItem();
            System.out.println("Selected stduent:\n" + selectedStudent);
        });
        return visibleStudentList;
    }
    
    private Label createStatusText(StringProperty stdntLstTxt) {
        Label statusText = new Label();
        statusText.setId("statusText");
        statusText.textProperty().bind(stdntLstTxt);
        //statusText.setFill(rgb(0, 0, 255));
        return statusText;
    }
    
    private Button createDirectoryButton(String filPath, Stage primaryStage) {
        FileChooser chooseList = new FileChooser();
        chooseList.setTitle("Select Student File");
        Button dirBtn = new Button("Set Student List");
        dirBtn.setId("dirBtn");
        dirBtn.setOnAction((final ActionEvent a) -> {
            File file = chooseList.showOpenDialog(primaryStage);
            if (file!=null) {
                createConfig(filPath, file.toString());
                restartApplication();
            }
        });
        return dirBtn;
    }
    
    private HBox createLayout(Label stdntLbl, TextField stdntFld,
            ListView<String> stdntListView, Button setDirBtn, Label statusText,
            Label fieldInstructions, Label listInstructions) {
        VBox studentInfoDiv1 = new VBox();
            studentInfoDiv1.getChildren().add(stdntLbl);
            studentInfoDiv1.getChildren().add(stdntFld);
        VBox studentInfoDiv2 = new VBox();
            studentInfoDiv2.setId("studentInfoDiv2");
            studentInfoDiv2.getChildren().add(studentInfoDiv1);
            studentInfoDiv2.getChildren().add(setDirBtn);
            studentInfoDiv2.getChildren().add(statusText);
            statusText.setTextOverrun(LEADING_ELLIPSIS);
        VBox studentInfoDiv3 = new VBox();
            studentInfoDiv3.setId("studentInfoDiv3");
            studentInfoDiv3.getChildren().add(fieldInstructions);
            fieldInstructions.setWrapText(true);
            studentInfoDiv3.getChildren().add(listInstructions);
            listInstructions.setWrapText(true);
        VBox studentInfoDiv4 = new VBox();
            studentInfoDiv4.setId("studentInfoDiv4");
            studentInfoDiv4.getChildren().add(studentInfoDiv2);
            studentInfoDiv4.getChildren().add(studentInfoDiv3);
        HBox layout = new HBox();
            layout.setId("layout");
            layout.getChildren().add(studentInfoDiv4);
            layout.getChildren().add(stdntListView);
        return layout;
    }
    
    private void createConfig(String filPath, String text) {
        try (Writer w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new File(filPath))))){
            w.write(text);
            w.close();
            } catch (IOException io) {
                    System.err.println(" IOException: " + io);
            }
    }
    
    public void restartApplication() {
        final String javaBin = System.getProperty("java.home")
                + File.separator + "bin" + File.separator + "java";
        try {
            final File currentJar = new File(CertificateCreator.class
                    .getProtectionDomain().getCodeSource().getLocation()
                    .toURI());
            if(!currentJar.getName().endsWith(".jar"))
            return;
            final ArrayList<String> command = new ArrayList<>();
            command.add(javaBin);
            command.add("-jar");
            command.add(currentJar.getPath());
            final ProcessBuilder builder = new ProcessBuilder(command);
            try{
                builder.start();
            } catch (IOException io) {
                System.out.println("I/O exception in restartApplication, "
                        + "program could not be restarted." + io);
            }
            System.exit(0);
            } catch (URISyntaxException uri) {
                System.out.println("URI sytanx exception in restartApplication: "
                        + uri);
        }
    }
        
    private String getDate() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        return date;
    }
        
    private String getStudentList(String filPath) {
        Path filePath = Paths.get(filPath);
        System.out.println("file path:\n" + filePath);
        String stdntLstPath = "";
        try{
            List<String> filPathLines = Files.readAllLines(filePath);
            if(filPathLines.isEmpty()) {   
            } else {
                stdntLstPath = filPathLines.get(0);
            }
            
        } catch (IOException io) {
            createConfig(filPath, "");
        }
        return stdntLstPath;
    }
    
    private void extractStudentList(List<String> studentList,
            String stdntLstPath) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(stdntLstPath)));
            String header = reader.readLine();
            while (reader.readLine() == "");
            String[] colNames = reader.readLine().split(" ");
            int firstNameInd = 0;
            int lastNameInd = 0;
            for(int i = 0; i < colNames.length; i++) {
                if (colNames[i].matches("First_Name")){
                    firstNameInd = i;
                }
                else if (colNames[i].matches("Last_Name")) {
                    lastNameInd = i;
                }
            }
            String str;
            int lines = 0;
            while ((str = reader.readLine()) != null) {
                String[] array = str.split(" ");
                studentList.add(array[firstNameInd]+ " " + array[lastNameInd]);
            }
        } catch(IOException io) {
            System.err.println("I/O Exception in method extractStudentList: "
                    + io);
        }
    }

}
