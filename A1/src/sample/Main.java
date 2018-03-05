//Colin Shaw 100628526
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import java.io.File;



public class Main extends Application {
    private TableView<TestFile> spamTable;


    private TextField accuracy, precision;



    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //choose the data file
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("."));
        File mainDirectory = directoryChooser.showDialog(primaryStage);


        GridPane layout = new GridPane();


        primaryStage.setTitle("Spam Master 9000");

        //this sets up the table
        TableColumn<TestFile, Integer> fileCol = new TableColumn<>("FILE");
        fileCol.setPrefWidth(350);
        fileCol.setCellValueFactory(new PropertyValueFactory<>("Filename"));


        TableColumn<TestFile, Integer> classCol = new TableColumn<>("Actual Class");
        classCol.setPrefWidth(100);
        classCol.setCellValueFactory(new PropertyValueFactory<>("ActualClass"));


        TableColumn<TestFile, Integer> probCol = new TableColumn<>("Spam Probability");
        probCol.setPrefWidth(150);
        probCol.setCellValueFactory(new PropertyValueFactory<>("SpamProbRounded"));




        spamTable = new TableView<>();
        spamTable.getColumns().add(fileCol);
        spamTable.getColumns().add(classCol);
        spamTable.getColumns().add(probCol);
        //this calls and runs Spamdetector and gets the list of TestFiles
        SpamDetector.main(new String[]{mainDirectory.getAbsolutePath()});
        ObservableList<TestFile> oListTestFile = FXCollections.observableArrayList(SpamDetector.getTestedFiles());
        spamTable.setItems(oListTestFile);

        layout.add(spamTable, 0,0);



        //calls the getAccuracy function
        double accuracy = SpamDetector.getAccuracy();
        Label accuracyLabel = new Label("Accuracy: " + accuracy);

        layout.add(accuracyLabel,0,1);


        //calls the getPrecision function
        double precision = SpamDetector.getPrecision();
        Label precisionLabel = new Label("Precision: " + precision);
        layout.add(precisionLabel,0,2);



        //Create the scene
        Scene scene = new Scene(layout, 600, 500);

        //Set the scene
        primaryStage.setScene(scene);
        primaryStage.show();
    }



}
