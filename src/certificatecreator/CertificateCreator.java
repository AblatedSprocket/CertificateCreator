/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package certificatecreator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.control.ScrollPane;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import static javafx.scene.paint.Color.rgb;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;

public class CertificateCreator extends Application {
    
    String selectedStudent;
    String date;
    String userPath;
    String resourcePath;
    @Override
    
    public void start(Stage primaryStage) {
        //Set class variables excluding selectedStudent.
        
        getDate();
        userPath = System.getProperty("user.dir");
        resourcePath = userPath + File.separator + "Resources";
        //Create resources folder if it does not exist.
        new File(resourcePath).mkdir();
        //Obtain properties from properties file or create a properties file if none exists.
        Properties properties = new Properties();
        establishProperties(properties);
        
        List<String> studentList = new ArrayList<>();
        //Create GUI.
        Scene scene = new Scene(createLayout(studentList, properties, primaryStage));
        scene.getStylesheets().add(CertificateCreator.class.getResource(
                "Main.css").toExternalForm());
        primaryStage.setTitle("Certificate Creator");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
    
    private void getDate() {
        //Obtains current date and formats it.
        
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = Calendar.getInstance();
        date = dateFormat.format(cal.getTime());
    }
    
    
    private ListView<String> createVisibleStudentList(List<String> studentList,
            TextField studentField, Properties properties) {
        //Creates visible list for GUI
        
        //Create internal ObservableList based on studentList.
        ObservableList<String> observableStudentList = FXCollections.observableList(studentList);
        //Initialize uneditable list visible in GUI.
        ListView<String> visibleStudentList = new ListView<>();
        visibleStudentList.setEditable(false);
        //Initialize FilteredList whose items come from observableStudentList
        FilteredList<String> filteredItems = new FilteredList<String>(
                observableStudentList, p -> true);
        //Enable filtering of visibleStudentList based on entry in TextField studentField
        studentField.textProperty().addListener((obs, oldVal, newVal) -> {
            final String selected = visibleStudentList.getSelectionModel()
                    .getSelectedItem();
            Platform.runLater(() -> {
                filteredItems.setPredicate(boxListItem -> {
                    if(boxListItem.toUpperCase().startsWith(newVal.
                            toUpperCase())) {
                        return true;
                    } else {
                        return false;
                    }
                });
            });
        });
        //As filteredItems updates, visibleStudentList does also.
        visibleStudentList.setItems(filteredItems);
        //Initiate certificate creation process upon click on list item.
        visibleStudentList.setOnMouseClicked((MouseEvent event) -> {
            selectedStudent = visibleStudentList.getSelectionModel()
                    .getSelectedItem();
            if (selectedStudent != null) {
                //Have user confirm certificate creation.
                Alert createCertificateAlert = new Alert(
                        AlertType.CONFIRMATION, "Create student certificate for "
                                + selectedStudent + "?");
                createCertificateAlert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        Path path = Paths.get(userPath + File.separator
                                + selectedStudent + " Certificate.docx");
                        String tempPath = properties.getProperty("templatePath");
                        //Check if certificate already exists, warn user if so.
                        if (Files.exists(path)) {
                            Alert overwriteExistingAlert = new Alert (
                                    AlertType.WARNING, selectedStudent
                                            + " Certificate.docx exists. "
                                            + "Overwrite?");
                            overwriteExistingAlert.showAndWait().ifPresent(
                                    confirmOverwrite -> {
                                //Overwrite certificate if user confirms.      
                                if (confirmOverwrite == ButtonType.OK) {
                                    try{
                                        Files.deleteIfExists(path);
                                        createStudentCertificate(tempPath);
                                    } catch(IOException io) {
                                        System.err.println("Could not delete "
                                                + "existing certificate. New "
                                                + "certificate was not "
                                                + "generated. " + io);
                                    }
                                }
                            });
                        } else {
                            createStudentCertificate(tempPath);
                        }

                    }
                });
                
            }
        });
        return visibleStudentList;
    }
    
    
    private Label createStatusText(StringProperty inputTxt) {
        //Creates and binds TextProperties for GUI status messages.
        
        Label statusText = new Label();
        statusText.setId("statusText");
        statusText.textProperty().bind(inputTxt);
        return statusText;
    }
    
    
    private void extractStudentList(List<String> studentList,
            String stdntLstPath) {
        //extracts student list from text file.
        
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
                    studentList.add(array[firstNameInd]+ " "
                            + array[lastNameInd]);
                }
            }
        } catch(IOException io) {
            System.err.println("I/O Exception in method extractStudentList: "
                    + io);
            Alert studentListNotFound = new Alert(AlertType.WARNING,
                    "Specified student list was not found. Please choose a different list.");
            studentListNotFound.showAndWait();
        } catch (NullPointerException npe) {
            System.err.println("Method copy failed:" + npe);
            Alert templateNotFound = new Alert(AlertType.WARNING,
                    "Specified student list was not found. " +
                            "Please choose a different list.");
            templateNotFound.showAndWait();
        }
    }
    
    
    private void establishProperties(Properties properties) {
        //Retrieves properties or creates properties file if none exists.
        
        //define properties file path.
        Path propPath = Paths.get(resourcePath + File.separator +
                "data.properties");
        //If file does not exist, create it.
        if (Files.notExists(propPath)) {
            try {
                Files.createFile(propPath);
            } catch (IOException io) {
                System.err.println("Could not create properties file: " + io);
            }
            //Set property values to pass to created file.
            String studentListPath = "";
            String templatePath = "";
            properties.setProperty("studentListPath", studentListPath);
            properties.setProperty("templatePath", templatePath);
            //Write properties to created properties file.
            try (FileOutputStream writeProp = new FileOutputStream(
                    resourcePath + File.separator + "data.properties")) {
                properties.store(writeProp, null);
            } catch (IOException io) {
            System.err.println("Could not write properties to file: " +io);
            }
        }
        else {
            //Load properties from existing properties file.
            try {
                properties.load(new FileInputStream(propPath.toString()));
            } catch (IOException io) {
                System.err.println("Could not load properties: " + io);
            }
        }
    }
    
    
    private VBox createLayout(List<String> studentList, Properties properties,
            Stage primaryStage) {
        //Creates GUI elements and layout.
        
        //Create elements for studentList status text.
        String stdntLstPath = properties.getProperty("studentListPath");
        StringProperty stdntLstTxt = new SimpleStringProperty();
        Label listStatusText = createStatusText(stdntLstTxt);
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
            listStatusText.setTextFill(rgb(255, 0, 0));
        }
        else {
            stdntLstTxt.set("Selected Student List:\n" + stdntLstPath);
            extractStudentList(studentList, stdntLstPath);
        }
        //Create elements for template status text.
        String templatePath = properties.getProperty("templatePath");
        StringProperty templateTxt = new SimpleStringProperty();
        Label tempStatusText = createStatusText(templateTxt);
        if (templatePath.isEmpty()) {
            templateTxt.set("No template has been selected");
            tempStatusText.setTextFill(rgb(255, 0, 0));
        }
        else {
            templateTxt.set("Selected template file:\n" + templatePath);
        }
        //Create elements for left half of GUI.
        Label stdntLbl = new Label("Student Name:");
        TextField stdntFld = new TextField();
        ListView<String> visibleStudentList = createVisibleStudentList(studentList,
                stdntFld, properties);
        Button setDirBtn = createDirectoryButton(properties, primaryStage);
        Button setTempBtn = createChooseTemplateButton(properties, templateTxt,
                tempStatusText, primaryStage);
        //Place elements in multiple VBoxes for positioning purposes.
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
        templateInfo.setId("studentInfo");
        templateInfo.getChildren().add(setTempBtn);
        templateInfo.getChildren().add(tempStatusText);
        tempStatusText.setTextOverrun(LEADING_ELLIPSIS);
        VBox userInput = new VBox();
        userInput.setId("userInput");
        userInput.getChildren().add(directoryInfo);
        userInput.getChildren().add(templateInfo);
        VBox visibleStudents = new VBox();
        //Create elements for right half of GUI.
        Label studentListLbl = new Label("Student List:");
        visibleStudents.getChildren().add(studentListLbl);
        visibleStudents.getChildren().add(visibleStudentList);
        //Combine left and right halves of GUI in HBox.
        HBox content = new HBox();
        content.setId("content");
        content.getChildren().add(userInput);
        content.getChildren().add(visibleStudents);
        //Create MenuBar for access to readme file.
        MenuBar menuBar = new MenuBar();
        //Create File menu item.
        Menu menuFile = new Menu("File");
        //Create readme menu item under file.
        MenuItem readMeMenu = createReadMeMenuItem(primaryStage);
        menuFile.getItems().add(readMeMenu);
        //Create close menu item under readme
        MenuItem closeMenu = createCloseMenuItem();
        menuFile.getItems().add(closeMenu);
        //Add File menu to menuBar.
        menuBar.getMenus().add(menuFile);
        VBox layout = new VBox();
        layout.getChildren().add(menuBar);
        layout.getChildren().add(content);

        return layout;
    }
    
    
    private MenuItem createReadMeMenuItem(Stage primaryStage) {
        //Create menu item for readme.
        
        MenuItem readMeMenu = new MenuItem("ReadMe");
        //Make readme window open when menu item is clicked.
        readMeMenu.setOnAction((ActionEvent event) -> {
            final Stage readMeStage = new Stage();
            readMeStage.initModality(Modality.APPLICATION_MODAL);
            readMeStage.initOwner(primaryStage);
            readMeStage.setTitle("ReadMe");
            VBox dialogVbox = new VBox(20);
            //Read in text from readme source file.
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    getClass().getResourceAsStream("readme.txt")));
            StringBuilder readMeLines = new StringBuilder();
            try {
                getText(readMeLines, br);
            } catch (IOException io) {
                System.err.println("Could not read readme file: " + io);
            }
            //Add text to readme window
            Text readMeContent = new Text(readMeLines.toString());
            readMeContent.setWrappingWidth(400);
            ScrollPane readMePane = new ScrollPane();
            readMePane.setContent(readMeContent);
            dialogVbox.getChildren().add(readMePane);
            Scene dialogScene = new Scene(dialogVbox, 420, 420);
            readMeStage.setScene(dialogScene);
            readMeStage.show();
        });
        return readMeMenu;
    }
    
    
    private MenuItem createCloseMenuItem() {
        //Create menu item to exit application.
        
        MenuItem closeMenu = new MenuItem("Close");
        closeMenu.setOnAction((ActionEvent event) -> {
            System.exit(0);
        });
        return closeMenu;
    }
    
    
    private Button createDirectoryButton(Properties properties,
            Stage primaryStage) {
        //Create button to choose student list directory.

        //Create button.
        Button dirBtn = new Button("Set Student List");
        dirBtn.setId("dirBtn");
        //Prompt user to choose student list file.
        FileChooser chooseList = new FileChooser();
        chooseList.setInitialDirectory(new File(resourcePath));
        chooseList.setTitle("Select Student List File");
        dirBtn.setOnAction((final ActionEvent a) -> {
            File listFile = chooseList.showOpenDialog(primaryStage);
            //Perform actions only if a new list is chosen.
            if (listFile!=null) {
                //Modify properties file to reflect change in path of student list file.
                properties.setProperty("studentListPath", listFile.toString());
                try {
                FileOutputStream saveProp = new FileOutputStream(resourcePath
                        + File.separator + "data.properties");
                try {
                    properties.store(saveProp, null);
                }catch (IOException io) {
                    System.err.println("Could not update properties: " + io);
                }
                restartApplication();
                }catch (FileNotFoundException fnf) {
                    System.err.println("Could not find save to properties file: " 
                            + fnf);
                }
            }
        });
        return dirBtn;
    }
    
    private Button createChooseTemplateButton(Properties properties,
            StringProperty templateTxt, Label tempStatusText,
            Stage primaryStage) {
        //Create button to choose certificate template.
        
        //Create button.
        Button chooseTemplateBtn = new Button("Set Certificate Template");
        chooseTemplateBtn.setId("chooseTemplateBtn");
        //Prompt user to choose a template file.
        FileChooser chooseTemplate = new FileChooser();
        chooseTemplate.setInitialDirectory(new File(resourcePath));
        chooseTemplate.setTitle("Select Certificate Template");
        chooseTemplateBtn.setOnAction((final ActionEvent a) -> {
            File templateFile = chooseTemplate.showOpenDialog(primaryStage);
            if (templateFile!=null) {
                String tempPath = templateFile.toString();
                String extension = tempPath.substring(
                        tempPath.lastIndexOf(".") + 1, tempPath.length());
                //Check if template is a .docx file. Abort and throw a warning if not.
                if ("docx".equals(extension)) {
                    properties.setProperty("templatePath", templateFile.toString());
                    templateTxt.set("Selected template file:\n"
                            + templateFile.toString());
                    tempStatusText.setTextFill(rgb(0, 0, 0));
                    try {
                        FileOutputStream saveProp = new FileOutputStream(resourcePath
                                + File.separator + "data.properties");
                        try {
                            properties.store(saveProp, null);
                        }catch (IOException io) {
                            System.err.println("Could not update properties: "
                                    + io);
                        }
                    }catch (FileNotFoundException fnf) {
                        System.err.println("Could not create output stream to "
                                + "properties file: " + fnf);
                    }
                }else {
                    Alert badTypeAlert = new Alert (
                        AlertType.WARNING, "Selected certificate file is not "
                            + "of proper format. Please choose another file "
                            + "or consult ReadMe for more information.");
                    badTypeAlert.showAndWait();
                }
            } 
        });
        return chooseTemplateBtn;
    }
    
    
    private void createStudentCertificate(String tempPath) {
        //Creates student certificate.
        
        //Read in template file and define output file.
        File inFile = new File(tempPath);
        File outFile = new File(userPath + File.separator + selectedStudent + 
                " Certificate.docx");
        //Create output file. Throw alert if tempPath references nonexisting file.
        try {
            copy(inFile, outFile);
            //Create internal word document based on copied template file. Write content.
            try {
                XWPFDocument certificate = new XWPFDocument(
                        new FileInputStream(outFile));
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
                //Write internal document to copied templated file.
                try {
                    FileOutputStream out = new FileOutputStream(
                            outFile.toString());
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
        } catch (IOException io) {
            System.err.println("Method copy failed:" + io);
        } catch (NullPointerException npe) {
            System.err.println("Method copy failed:" + npe);
            Alert templateNotFound = new Alert(AlertType.WARNING,
                    "Specified certificate template was not found. " +
                            "Please choose a different template.");
            templateNotFound.showAndWait();
        }
    }
    
    
    private void getText(StringBuilder readMeLines, BufferedReader br)
            throws IOException {
        //Retrieves content from readme file.
        
        int i = 0;
        String line = "";
        
        while (line != null) {
            readMeLines.append(line).append("\n");
            line = br.readLine();
        }
    }
    
    
    private static void copy (File source, File target)
            throws IOException, NullPointerException {
        //Copies template file to student certificate.
        
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
        //Restarts application
        
        
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
