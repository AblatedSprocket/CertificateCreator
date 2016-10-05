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
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
        }
        else {
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            ArrayList<String[]> studentList = new ArrayList<String[]>();
            extractStudentList(studentList, stdntLstPath);
        }
        Label stdntLbl = new Label("Student:");
        TextField stdntFld = new TextField();
        Text dirStsTxt = new Text();
        dirStsTxt.textProperty().bind(stdntLstTxt);
        Button setDirBtn = createDirectoryButton(stdntLstTxt, configPath,
                primaryStage);
        
        Scene scene = new Scene(createLayout(stdntLbl, stdntFld, setDirBtn,
                dirStsTxt));
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Writer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }
    private void extractStudentList(ArrayList<String[]> studentList,
            String stdntLstPath) {
        ArrayList<String[]> rawStudentList = new ArrayList<String[]>();
        String str;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(stdntLstPath));
            String header = reader.readLine();
            while (reader.readLine() == "");
            
            String[] colNames = reader.readLine().split(" ");
            System.out.println("Column names:");
            int firstNameInd = 0;
            int lastNameInd = 0;
            for(int i = 0; i < colNames.length; i++) {
                System.out.println(colNames[i]);
                if (colNames[i].matches("First_Name")){
                    firstNameInd = i;
                }
                else if (colNames[i].matches("Last_Name")) {
                    lastNameInd = i;
                }
            }
            
            System.out.println("First name index is:" + firstNameInd +
                    "\nLast name index is:" + lastNameInd);
            while ((str = reader.readLine()) != null) {
                String[] array = str.split(" ");
                
                System.out.println("Str is now:\n" + str);
                System.out.println("Array is now:\n" + array);
                rawStudentList.add(array);
            }
            while ((str = reader.readLine()) != null) {
                String[] array = str.split(" ");
                
                System.out.println("Str is now:\n" + str);
                System.out.println("Array is now:\n" + array);
                rawStudentList.add(array);
            }
            System.out.println("Raw student list:\n" + rawStudentList);
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
    private GridPane createLayout(Label stdntLbl, TextField stdntFld, 
            Button setDirBtn, Text dirStsTxt) {
        GridPane layout = new GridPane();
        layout.setAlignment(Pos.CENTER);
        layout.add(stdntLbl, 1, 1);
        layout.add(stdntFld, 2, 1);
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
