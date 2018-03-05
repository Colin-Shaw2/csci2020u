//package sample;

//import sample;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;



public class Main extends Application {
    private TableView<TestFile> spamTable;

    //Our variables for the simple application
    private TextField accuracy, precision, _email, _phoneNum;
    private PasswordField _pw1;
    private DatePicker _dp1;
    private Button _btn1;



    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane layout = new GridPane();

        primaryStage.setTitle("A1");


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
        SpamDetector.main(new String[]{"SpamDetector"});
        ObservableList<TestFile> oListTestFile = FXCollections.observableArrayList(SpamDetector.getTestedFiles());
        spamTable.setItems(oListTestFile);

        layout.add(spamTable, 0,0);



        //Set our variables

        double accuracy = SpamDetector.getAccuracy();
        Label accuracyLabel = new Label("Accuracy: " + accuracy);
        //accuracy = new TextField();
        //accuracy.setPromptText("Accuracy");
        //accuracy.setDisable(true);

        layout.add(accuracyLabel,0,1);



        double precision = SpamDetector.getPrecision();
        Label precisionLabel = new Label("Precision: " + precision);
        //precision = new TextField();
        //precision.setPromptText("Precision");
        //precision.setDisable(true);
        layout.add(precisionLabel,0,2);



/*
        Label pwLabel = new Label("Password:");
        _pw1 = new PasswordField();
        _pw1.setPromptText("guest123");

        Label textLabel2 = new Label("Full Name:");
        _fullName = new TextField();
        _fullName.setPromptText("Jon Smith");


        Label textLabel3 = new Label("Email:");
        _email = new TextField();
        _email.setPromptText("Jon.Smith@gmail.com");


        Label textLabel4 = new Label("Phone #:");
        _phoneNum = new TextField();
        _phoneNum.setPromptText("###-###-####");



        Label dateLabel = new Label("Date of Birth");
        _dp1 = new DatePicker();
        _btn1 = new Button("Submit  ");
        //This set default button makes it so if there's one button on the form, we can press enter to press it
        _btn1.setDefaultButton(true);
        gp.add(_btn1, 0, 6);




        //This is out button event handler. We will work with what happens here
        _btn1.setOnAction(new EventHandler<ActionEvent>() {
                              @Override
                              public void handle(ActionEvent event) {
                                  //Append to our TextArea with our button
                                  System.out.println(_usrName.getText() + "\n" + _fullName.getText() + "\n" +
                                          _pw1.getText() + "\n" + _email.getText() + "\n" +
                                          _phoneNum.getText() + "\n" + _dp1.getValue() + "\n");

                                  //Clear previous inputs
                                  //_usrName.clear();
                                  //_pw1.clear();
                              }
                          }
        );
*/
        //Create the scene
        Scene scene = new Scene(layout, 600, 500);

        //Set the scene
        primaryStage.setScene(scene);
        primaryStage.show();
    }



}
