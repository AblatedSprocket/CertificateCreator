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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
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
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import static javafx.scene.control.OverrunStyle.LEADING_ELLIPSIS;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import static javafx.scene.paint.Color.rgb;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDocument1;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
/**
 *
 * @author Andy
 */

public class CertificateCreator extends Application {
    
    String selectedStudent;
    String date;
    String userPath;
    @Override
    public void start(Stage primaryStage) {
        //get current date
        getDate();
        //get user directory and config file directory
        userPath = System.getProperty("user.dir");
        String configPath = userPath + File.separator + "config.txt";
        System.out.println("config path:\n" + configPath);
        //Perform initial check for student list
        String stdntLstPath = getStudentList(configPath);
        StringProperty stdntLstTxt = new SimpleStringProperty();
        List<String> studentList = new ArrayList<>();
        Label statusText = createStatusText(stdntLstTxt);
        System.out.println("Directory path:\n" + stdntLstPath);
        if (stdntLstPath.isEmpty()) {
            stdntLstTxt.set("No student list has been selected");
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
                visibleStudentList, setDirBtn, statusText));
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
    
    private void getDate() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        Calendar cal = Calendar.getInstance();
        date = dateFormat.format(cal.getTime());
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
        visibleStudentList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                selectedStudent = visibleStudentList.getSelectionModel()
                        .getSelectedItem();
                if (selectedStudent != null) {
                     createStudentCertificate();
                }
            }
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
    
    private HBox createLayout(Label stdntLbl, TextField stdntFld,
            ListView<String> stdntListView, Button setDirBtn, Label statusText) {
        VBox studentName = new VBox();
        studentName.getChildren().add(stdntLbl);
        studentName.getChildren().add(stdntFld);
        VBox studentInfo = new VBox();
        studentInfo.setId("studentInfo");
        studentInfo.getChildren().add(studentName);
        studentInfo.getChildren().add(setDirBtn);
        studentInfo.getChildren().add(statusText);
        statusText.setTextOverrun(LEADING_ELLIPSIS);
        HBox layout = new HBox();
        layout.setId("layout");
        layout.getChildren().add(studentInfo);
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
    
    private void createStudentCertificate() {
        InputStream pic = this.getClass().getResourceAsStream("image1.png");
        XWPFDocument certificate = new XWPFDocument();
        try {
            byte[] picbytes = IOUtils.toByteArray(pic);
            try {
                certificate.addPictureData(picbytes, Document.PICTURE_TYPE_PNG);
            } catch (InvalidFormatException ife) {
                System.out.println("Picture was not added to document " +
                        "because of improper format: " + ife);
            }
            
        } catch (IOException io) {
            System.out.println("Could not create byte from image1.png");
        }
        
        File templateFile = new File(userPath + File.separator + "Template.docx");
        File outFile = new File(userPath + File.separator + selectedStudent + 
                " Certificate.docx");
        try {
            Files.copy(templateFile.toPath(), outFile.toPath(), COPY_ATTRIBUTES);
        } catch (IOException io) {
            System.out.println("Could not copy template file to output " +
                    "file: " + io);
        }
        
        XWPFParagraph p1 = certificate.createParagraph();
        p1.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun r1 = p1.createRun();
        r1.setFontFamily("Candara");
        r1.setFontSize(40);
        r1.setText("\n\nLawton Elementary Congratulates");
        XWPFRun r2 = p1.createRun();
        r2.setFontFamily("Candara");
        r2.setFontSize(36);
        r2.setBold(true);
        r2.setText("\n\n" + selectedStudent + "\n\n");
        XWPFRun r3 = p1.createRun();
        r3.setFontFamily("Candara");
        r3.setFontSize(26);
        r3.setText("For being a Lawton CARES winner on\n" + date + "\n\n\n\n");
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
                System.out.println("Could not write file: " + io);
            }
        } catch(FileNotFoundException fnf) {
            System.out.println("Could not find output file: " + fnf);
        }
        
//        CTDocument1 document = certificate.getDocument();
//        CTBackground background = document.getBackground();
//        background.newInputStream();
//        CTBody body = document.getBody();
//        CTSectPr section = body.getSectPr();
//        CTPageSz pageSize = section.getPgSz();
//        pageSize.setOrient(CTPageOrientation.LANDSCAPE);
//        pageSize.setW(BigInteger.valueOf(15840));
//        pageSize.setH(BigInteger.valueOf(12240));
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
                System.out.println("I/O exception, program could not be "
                        + "restarted." + io);
            }
            System.exit(0);
            } catch (URISyntaxException uri) {
                System.out.println("URI sytanx exception: " + uri);
        }
    }
}
