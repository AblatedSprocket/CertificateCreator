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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author Andy
 */
public class CertificateCreator extends Application {
    @Override
    
    public void start(Stage primaryStage) throws FileNotFoundException, IOException {
        Properties defaultProps = new Properties();
        String userPath = System.getProperty("user.dir");
        String configPath = userPath + File.separator + "config.txt";
        String stdntLstPath = getStudentList(configPath);
        StringProperty stdntLstTxt = new SimpleStringProperty();
        List<String> studentList = new ArrayList<String>();
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
        }
        else {
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            extractStudentList(studentList, stdntLstPath);
        }
        Label stdntLbl = new Label("Student Name:");
        TextField stdntFld = new TextField();
        Text dirStsTxt = new Text();
        ObservableList boxList = FXCollections.observableList(studentList);
        ListView<String> stdntListView = createListView(boxList);
        FilteredList<String> filteredItems = new FilteredList<String>(boxList, p -> true);
        stdntFld.textProperty().addListener((obs, oldVal, newVal) -> {
            final String selected = stdntListView.getSelectionModel().getSelectedItem();
            Platform.runLater(() -> {
                if (selected == null || !selected.equals(stdntFld.getText())) {
                    filteredItems.setPredicate(boxListItem -> {
                        if(boxListItem.toUpperCase().startsWith(newVal.toUpperCase())) {
                            System.out.println("Filtering based on " + newVal);
                            return true;
                        } else {
                            return false;
                        }
                    });
                }
            });
        });
        stdntListView.setItems(filteredItems);
        dirStsTxt.textProperty().bind(stdntLstTxt);
        Button setDirBtn = createDirectoryButton(stdntLstTxt, configPath,
                primaryStage);
        
        Scene scene = new Scene(createLayout(stdntLbl, stdntFld, stdntListView, setDirBtn,
                dirStsTxt), 500, 500);
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Writer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    private ListView<String> createListView(ObservableList<String> boxList) {
        ListView<String> stdntListView = new ListView<String>();
        stdntListView.setEditable(false);
        return stdntListView;
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
        System.out.println("Student list is now:\n");
        System.out.println(studentList);
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
            Button setDirBtn, Text dirStsTxt) {
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.add(stdntLbl, 1, 1);
        layout.add(stdntFld, 2, 1);
        layout.add(stdntListView, 3, 1);
        layout.add(setDirBtn, 2, 2);
        layout.add(dirStsTxt, 2, 3);
        return layout;
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    private void createConfig(String filPath, String text) {
        try {
            File file = new File(filPath);
            FileOutputStream os = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(os);
            Writer w = new BufferedWriter(osw);
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
    /* Program functionality
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
