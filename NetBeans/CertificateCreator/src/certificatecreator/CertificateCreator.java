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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import static javafx.scene.control.OverrunStyle.LEADING_ELLIPSIS;
import javafx.scene.layout.BorderPane;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import static javafx.scene.paint.Color.rgb;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import static javafx.scene.paint.Color.rgb;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
/**
 *
 * @author Andy
 */

public class CertificateCreator extends Application {
    
    String selectedStudent;
    String date;
    String userPath;
    String resourcePath;
    @Override
    public void start(Stage primaryStage) {
        //get current date
        getDate();
        Properties properties = new Properties();
        String stdntLstPath = "";
        
        //get user directory and config file directory
        userPath = System.getProperty("user.dir");
        resourcePath = userPath + File.separator + "Resources";
        new File(resourcePath).mkdir();
        String configPath = resourcePath + File.separator + "config.txt";
        String templatePath = resourcePath + File.separator + "templatePath.txt";
        checkProperties(properties);
        System.out.println("Properties:\n" + properties);
        stdntLstPath = properties.getProperty("studentListPath");
        templatePath = properties.getProperty("templatePath");
        StringProperty stdntLstTxt = new SimpleStringProperty();
        StringProperty templateTxt = new SimpleStringProperty();
        List<String> studentList = new ArrayList<>();
        Label listStatusText = createStatusText(stdntLstTxt);
        Label tempStatusText = createStatusText(templateTxt);
        System.out.println("Directory path:\n" + stdntLstPath);
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
            listStatusText.setTextFill(rgb(255, 0, 0));
        }
        else {
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            extractStudentList(studentList, stdntLstPath);
        }
        
        if (templatePath.isEmpty()) {
            templateTxt.set("No template has been selected");
            tempStatusText.setTextFill(rgb(255, 0, 0));
        }
        else {
            templateTxt.set("Selected template file:\n" + templatePath);
        }
        Label stdntLbl = new Label("Student Name:");
        TextField stdntFld = new TextField();
        ListView visibleStudentList = createVisibleStudentList(studentList,
                stdntFld, properties);
        Button setDirBtn = createDirectoryButton(configPath, properties,
                primaryStage);
        Button setTempBtn = createChooseTemplateButton(properties, primaryStage);
        Scene scene = new Scene(createLayout(stdntLbl, stdntFld,
                visibleStudentList, setDirBtn, listStatusText, setTempBtn, tempStatusText, primaryStage));
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Writer");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    
    private Label createReadMeLabel() {
        Label readMe = new Label("README");
        return readMe;
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    private void getDate() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = Calendar.getInstance();
        date = dateFormat.format(cal.getTime());
    }
    
    
    private ListView createVisibleStudentList(List<String> studentList,
            TextField stdntFld, Properties properties) {
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
                        if(boxListItem.toUpperCase().startsWith(newVal.
                                toUpperCase())) {
                            return true;
                        } else {
                            return false;
                        }
                    });
                }
            });
        });
        visibleStudentList.setItems(filteredItems);
        visibleStudentList.setOnMouseClicked((MouseEvent event) -> {
            selectedStudent = visibleStudentList.getSelectionModel()
                    .getSelectedItem();
            if (selectedStudent != null) {
                String tempPath = properties.getProperty("templatePath");
                String extension = tempPath.substring(
                        tempPath.lastIndexOf(".") + 1, tempPath.length());
                if ("docx".equals(extension)) {
                    createStudentCertificate(tempPath);
                } else {
                    
                }
            }
        });
        return visibleStudentList;
    }
    
    
    private Label createStatusText(StringProperty inputTxt) {
        Label statusText = new Label();
        statusText.setId("statusText");
        statusText.textProperty().bind(inputTxt);
        return statusText;
    }
    
    
    private void extractStudentList(List<String> studentList,
            String stdntLstPath) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(stdntLstPath)));
            String header = reader.readLine();
            if (header != null) {
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
            }
        } catch(IOException io) {
            System.err.println("I/O Exception in method extractStudentList: "
                    + io);
        }
    }
    
    
    private void checkProperties(Properties properties) {
        Path propPath = Paths.get(resourcePath + File.separator +
                "data.properties");
        if (Files.notExists(propPath)){

            try {
                Files.createFile(propPath);
            } catch (IOException io) {
                System.err.println("Could not create properties file: " + io);
            }
            String studentListPath = "";
            String templatePath = "";
            properties.setProperty("studentListPath", studentListPath);
            properties.setProperty("templatePath", templatePath);
            try {
                try (FileOutputStream writeProp = new FileOutputStream(resourcePath +
                        File.separator + "data.properties")) {
                    properties.store(writeProp, null);
                }
                
            } catch (IOException io) {
                System.err.println("Could not write properties to file: " +io);
            }
        }
        else {
            try {
                properties.load(new FileInputStream(propPath.toString()));
            } catch (IOException io) {
                System.err.println("Could not load properties: " + io);
            }
        }
    }
    
    
    private String getStudentList(String filPath) {
        Path filePath = Paths.get(filPath);
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
    
    
    private VBox createLayout(Label stdntLbl, TextField stdntFld,
            ListView<String> stdntListView, Button setDirBtn,
            Label listStatusText, Button setTempBtn, Label tempStatusText, Stage primaryStage) {
        VBox studentName = new VBox();
        studentName.getChildren().add(stdntLbl);
        studentName.getChildren().add(stdntFld);
        VBox directoryInfo = new VBox();
        directoryInfo.setId("studentInfo");
        directoryInfo.getChildren().add(studentName);
        directoryInfo.getChildren().add(setDirBtn);
        directoryInfo.getChildren().add(listStatusText);
        listStatusText.setTextOverrun(LEADING_ELLIPSIS);
        VBox templateInfo = new VBox();
        templateInfo.getChildren().add(setTempBtn);
        templateInfo.getChildren().add(tempStatusText);
        tempStatusText.setTextOverrun(LEADING_ELLIPSIS);
        VBox userInput = new VBox();
        userInput.setId("userInput");
        userInput.getChildren().add(directoryInfo);
        userInput.getChildren().add(templateInfo);
        HBox content = new HBox();
        content.setId("layout");
        content.getChildren().add(userInput);
        content.getChildren().add(stdntListView);
        VBox layout = new VBox();
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem readMe = new MenuItem("Readme");
        readMe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(primaryStage);
                dialog.setTitle("Readme");
                VBox dialogVbox = new VBox(20);
                dialogVbox.getChildren().add(new Text("This is a Dialog"));
                Scene dialogScene = new Scene(dialogVbox, 300, 200);
                dialog.setScene(dialogScene);
                dialog.show();
            }
         });
        menuFile.getItems().add(readMe);
        menuBar.getMenus().add(menuFile);
            layout.getChildren().add(menuBar);
            layout.getChildren().add(content);

        return layout;
    }
    
    
    private void createConfig(String filPath, String text) {
        try {
            try (Writer w = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(filPath))))) {
                w.write(text);
            }
            } catch (IOException io) {
                    System.err.println("Could not write to config file: " + io);
            }
    }
    
    
    private Button createDirectoryButton(String filPath, Properties properties, Stage primaryStage) {
        FileChooser chooseList = new FileChooser();
        chooseList.setTitle("Select Student File");
        Button dirBtn = new Button("Set Student List");
        dirBtn.setId("dirBtn");
        dirBtn.setOnAction((final ActionEvent a) -> {
            File listFile = chooseList.showOpenDialog(primaryStage);
            if (listFile!=null) {
                //createConfig(filPath, file.toString());
                properties.setProperty("studentListPath", listFile.toString());
                try {
                FileOutputStream saveProp = new FileOutputStream(resourcePath + File.separator + "data.properties");
                try {
                    properties.store(saveProp, null);
                }catch (IOException io) {
                    System.err.println("Could not update properties: " + io);
                }
                restartApplication();
                }catch (FileNotFoundException fnf) {
                    System.err.println("Could not find save to properties file: " +fnf);
                }
            }
        });
        return dirBtn;
    }
    
    private Button createChooseTemplateButton(Properties properties, StringProperty templateTxt, Stage primaryStage) {
        FileChooser chooseTemplate = new FileChooser();
        chooseTemplate.setTitle("Select Certificate Template");
        Button chooseTemplateBtn = new Button("Set Certificate Template");
        chooseTemplateBtn.setId("chooseTemplateBtn");
        chooseTemplateBtn.setOnAction((final ActionEvent a) -> {
            File templateFile = chooseTemplate.showOpenDialog(primaryStage);
            if (templateFile!=null) {
                //createConfig(filPath, file.toString());
                properties.setProperty("templatePath", templateFile.toString());
                templateTxt.set("Selected template file:\n" + templateFile.toString());
                try {
                FileOutputStream saveProp = new FileOutputStream(resourcePath + File.separator + "data.properties");
                try {
                    properties.store(saveProp, null);
                }catch (IOException io) {
                    System.err.println("Could not update properties: " + io);
                }
                }catch (FileNotFoundException fnf) {
                    System.err.println("Could not find save to properties file: " +fnf);
                }
            }
        });
        return chooseTemplateBtn;
    }
    
    
    private void createStudentCertificate(String tempPath) {
        File inFile = new File(tempPath);
        File outFile = new File(userPath + File.separator + selectedStudent + 
                " Certificate.docx");
        try {
            copy(inFile, outFile);
        } catch (IOException io) {
            System.err.println("Method copy failed:" + io);
        }
        try {
            XWPFDocument certificate = new XWPFDocument(new FileInputStream(outFile));
            XWPFParagraph p1 = certificate.createParagraph();
            p1.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun r1 = p1.createRun();
            r1.setFontFamily("Candara");
            r1.setFontSize(40);
            r1.addBreak();
            r1.setText("Lawton Elementary Congratulates");
            XWPFRun r2 = p1.createRun();
            r2.setFontFamily("Candara");
            r2.setFontSize(36);
            r2.setBold(true);
            r2.addBreak();
            r2.addBreak();
            r2.setText(selectedStudent);
            r2.addBreak();
            r2.addBreak();
            XWPFRun r3 = p1.createRun();
            r3.setFontFamily("Candara");
            r3.setFontSize(26);
            r3.setText("For being a Lawton CARES winner on");
            r3.addBreak();
            r3.setText(date);
            r3.addBreak();
            r3.addBreak();
            r3.addBreak();
            r3.addBreak();
            XWPFRun r4 = p1.createRun();
            r4.setColor("5B9BD5");
            r4.setFontFamily("Candara");
            r4.setFontSize(26);
            r4.setText("Compassion+Attitude+Respect+Effort+Safety=CARES");
            try {
                FileOutputStream out = new FileOutputStream(outFile.toString());
                try {
                    certificate.write(out);
                    out.close();
                } catch (IOException io) {
                    System.err.println("Could not write file: " + io);
                }
            } catch(FileNotFoundException fnf) {
                System.err.println("Could not find output file: " + fnf);
            }
        } catch (IOException io) {
            System.err.println("Copy of template could not be found: " + io);
        }
    }
    
    
    private static void copy (File source, File target) throws IOException {
        FileChannel sourceChannel = null;
        FileChannel targetChannel = null;
        try {
            sourceChannel = new FileInputStream(source).getChannel();
            targetChannel = new FileOutputStream(target).getChannel();
            targetChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally{
            targetChannel.close();
            sourceChannel.close();
        }
    }
    
    
    private void restartApplication() {
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
                System.err.println("I/O exception, program could not be "
                        + "restarted." + io);
            }
            System.exit(0);
            } catch (URISyntaxException uri) {
                System.err.println("URI syntax exception: " + uri);
        }
    }
}
