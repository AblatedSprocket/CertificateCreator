/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificatecreator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import static javafx.scene.paint.Color.rgb;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
        //Perform initial check for student list
        String stdntLstPath = getStudentList(configPath);
        StringProperty stdntLstTxt = new SimpleStringProperty();
        BooleanProperty hasDir = new SimpleBooleanProperty(false);
        List<String> studentList = new ArrayList<>();
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
        }
        else {
            hasDir.set(true);
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            extractStudentList(studentList, stdntLstPath);
        }
        Label stdntLbl = new Label("Student Name:");
        TextField stdntFld = new TextField();
        Text statusText = createStatusText(stdntLstTxt);
        ListView visibleStudentList = createVisibleStudentList(studentList,
                stdntFld);
        
        Button setDirBtn = createDirectoryButton(stdntLstTxt, configPath,
                primaryStage);
        
        Scene scene = new Scene(createLayout(stdntLbl, stdntFld,
                visibleStudentList, setDirBtn, statusText));
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Writer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    private String getDate() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = Calendar.getInstance();
        String date = dateFormat.format(cal.getTime());
        return date;
    }
    private ListView createVisibleStudentList(List<String> studentList, TextField stdntFld) {
        ObservableList boxList = FXCollections.observableList(studentList);
        ListView<String> visibleStudentList = new ListView<>();
        visibleStudentList.setEditable(false);
        FilteredList<String> filteredItems = new FilteredList<>(boxList, p -> true);
        stdntFld.textProperty().addListener((obs, oldVal, newVal) -> {
            final String selected = visibleStudentList.getSelectionModel().getSelectedItem();
            Platform.runLater(() -> {
                if (selected == null || !selected.equals(stdntFld.getText())) {
                    filteredItems.setPredicate(boxListItem -> {
                        if(boxListItem.toUpperCase().startsWith(newVal.toUpperCase())) {
                            return true;
                        } else {
                            return false;
                        }
                    });
                }
            });
        });
        visibleStudentList.setItems(filteredItems);
        visibleStudentList.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                selectedStudent = visibleStudentList.getSelectionModel().getSelectedItem();
            }
        });
        return visibleStudentList;
        
    }
    private Text createStatusText(StringProperty stdntLstTxt) {
        Text statusText = new Text();
        statusText.textProperty().bind(stdntLstTxt);
        //statusText.setFill(rgb(0, 0, 255));
        return statusText;
    }
    
    /*private int findIndex(String arg, String[] list) {
        boolean comp = true;
        int listInd = 0;
        while(comp) {
            if (list[listInd] == arg){
                comp = false;
            }
            listInd++;
        }
        return listInd;
    }*/
    private void extractStudentList(List<String> studentList,
            String stdntLstPath) {
        
        String str;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(stdntLstPath));
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
    private String getStudentList(String filPath) {
        Path filePath = Paths.get(filPath);
        String stdntLstPath = "";
        try{
            List<String> filPathLines = Files.readAllLines(filePath);
            stdntLstPath = filPathLines.get(0);
            
        } catch (IOException io) {
            createConfig(filPath, "");
        }
        return stdntLstPath;
    }
    
    private GridPane createLayout(Label stdntLbl, TextField stdntFld, ListView<String> stdntListView, 
            Button setDirBtn, Text statusTxt) {
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.add(stdntLbl, 1, 1);
        layout.add(stdntFld, 2, 1);
        layout.add(stdntListView, 3, 1);
        layout.add(setDirBtn, 2, 2);
        layout.add(statusTxt, 2, 3);
        return layout;
    }
    
    public static void main(String[] args) {
        launch(args);
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
    Button createDirectoryButton(StringProperty stdntLstTxt, String filPath, Stage primaryStage) {
        FileChooser chooseList = new FileChooser();
        chooseList.setTitle("Select Student File");
        Button dirBtn = new Button("Set Student List");
        dirBtn.setId("dirBtn");
        dirBtn.setOnAction(new EventHandler<ActionEvent> (){
            @Override
            public void handle(final ActionEvent e){
                File file = chooseList.showOpenDialog(primaryStage);
                if (file!=null) {
                    stdntLstTxt.set("Selected student file:\n" + file);
                    createConfig(filPath, file.toString());
                }
            }
        });
        
        return dirBtn;
    }
    /*// Program functionality
    public HSSFSheet getSheet(String fid) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(fid));
        } catch(FileNotFoundException e) {
            System.err.println("Could not locate requested file. Please ensure"
                    + "the following steps have been taken:\n"
                    + "File has been specified correctly\n"
                    + "Program has been placed in proper directory\n"
                    + "If the above steps do not resolve the problem, please"
                    + "contact Laura Betz.");
        }
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        HSSFSheet sheet = wb.getSheetAt(0);
        return sheet;
    }
    
        int rows = sheet.getLastRowNum();
        String name = getName();
        int rowInd = 0;
        do{
            
        }while(rowInd <= rows);
    public String getName(){
        
    }*/
    
}
