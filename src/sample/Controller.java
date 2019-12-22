package sample;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.util.Callback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    int currentId;
    @FXML
    private TableView<Table> table;
    @FXML
    private TableColumn<Table, String> group;
    @FXML
    private TableColumn<Table, String> ID;
    @FXML
    private TableColumn<Table, String> name;

    @FXML
    private TableColumn<Table, String> lastName;

    @FXML
    private TableColumn<Table, String> attendance;
    @FXML
    private JFXTextField groupField;

    @FXML
    private JFXTextField nameField;

    @FXML
    private JFXTextField lastNameField;
    @FXML
    private ComboBox <String> comboBox, toFileBox;
    @FXML
    private JFXDatePicker oneDayDatePicker, getStartDayPicker,endDayPicker;
    @FXML
    private JFXTextField daysPeriod;
    LocalDate end,start,oneDay;
    LocalDate period;
    @FXML
    private JFXComboBox<String> deleteBox;

    String groupString, nameString, lastNameString;
    int trueStatements = 0; //to check if all input statements were correct


    ObservableList<Table> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            DBUtils.dbConnect();
            try (ResultSet rs = DBUtils.dbExecuteQuery("Select * from student")) {
                while (rs.next()) {
                    data.add(new Table(rs.getInt("ID"), rs.getString("Course"), rs.getString("Name"), rs.getString("LastName"),
                            rs.getString("Email")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        ID.setCellValueFactory(new PropertyValueFactory<>("ID"));
        group.setCellValueFactory(new PropertyValueFactory<>("group"));
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        attendance.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        try {
            comboBox.setItems(StudentsDataAccess.distinctCourse());
            ObservableList<String> options = FXCollections.observableArrayList("To xls file", "To csv file", "To pdf file", "From xls file", "From csv file");
            ObservableList<String> deleteOptions = FXCollections.observableArrayList("Delete everything", "Delete selected row");
            deleteBox.setItems(deleteOptions);
            toFileBox.setItems(options);
            showStudents(data);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void onTablePressed(MouseEvent event) throws SQLException, ClassNotFoundException {
       Table tb = table.getSelectionModel().getSelectedItem();
       groupField.setText(tb.getGroup());
       nameField.setText(tb.getName());
       lastNameField.setText(tb.getLastName());
       currentId = tb.getID();

    }
    @FXML
    void onUpdatePressed(MouseEvent event) throws SQLException, ClassNotFoundException {
        int val = validation();
        if(val==3){
            StudentsDataAccess.updateStudent(currentId,groupString,nameString,lastNameString);
            data = StudentsDataAccess.searchStudents();
            showStudents(data);
        }
    }
    @FXML
    void onDeletePressed(MouseEvent event) {
        deleteBox.setOnAction(actionEvent -> {
            try{
                String deleteMethodSelected = deleteBox.getValue();
                if(!deleteBox.isDisable()) {
                    switch (deleteMethodSelected) {
                        case "Delete everything":
                            DBUtils.dbExecuteUpdate("Delete from student");
                            data = StudentsDataAccess.searchStudents();
                            showStudents(data);
                            break;
                        case "Delete selected row":
                            Table id = table.getSelectionModel().getSelectedItem();
                            StudentsDataAccess.deleteStudentWithId(id.getID());
                            data = StudentsDataAccess.searchStudents();
                            showStudents(data);
                            break;
                        default:
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
       deleteBox.setValue("Delete");
    }
    @FXML
    void endDayAttendancePressed(MouseEvent event) {
        endDayPicker.setOnAction(actionEvent -> {
            end = endDayPicker.getValue();
        });
    }

    @FXML
    void oneDayAttendancePressed(MouseEvent event) {
        oneDayDatePicker.setOnAction((actionEvent -> {
            oneDay = oneDayDatePicker.getValue();
        }));
    }

    @FXML
    void periodAttendancePressed(ActionEvent event) {
        if(daysPeriod.getText().matches("//d")) {
            period = LocalDate.parse(daysPeriod.getText());
        }
    }

    @FXML
    void startDayAttendancePressed(MouseEvent event) {
        getStartDayPicker.setOnAction(actionEvent -> {
            start = getStartDayPicker.getValue();
        });
    }

    @FXML
    void comboBoxPressed(MouseEvent event) throws SQLException, ClassNotFoundException {
        //comboBox.getButtonCell().backgroundProperty().set("white");//setStyle("-fx-text-fill: white");
        comboBox.getButtonCell().setStyle(" -fx-prompt-text-fill: white");
        comboBox.setOnAction(actionEvent -> {
            try {
                String courseSelected = comboBox.getValue();
                if(courseSelected == "All courses"){
                    data = StudentsDataAccess.searchStudents();
                    table.setItems(data);
                }
                else{
                    searchBy(event,courseSelected);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    void toFileBoxPressed(MouseEvent event) {
        toFileBox.setOnAction(actionEvent -> {
            try{
                String methodSelected = toFileBox.getValue();
                switch (methodSelected){
                    case "To xls file":
                        StudentsDataAccess.exportToExcel();
                        comboBox.setItems(StudentsDataAccess.distinctCourse());
                        break;
                    case "To csv file":
                        StudentsDataAccess.exportToCSV();
                        comboBox.setItems(StudentsDataAccess.distinctCourse());
                        break;
                    case "To pdf file":
                        StudentsDataAccess.exportToPDF();
                        comboBox.setItems(StudentsDataAccess.distinctCourse());
                        break;
                    case "From xls file":
                        StudentsDataAccess.importFromExcel();
                        data = StudentsDataAccess.searchStudents();
                        showStudents(data);
                        comboBox.setItems(StudentsDataAccess.distinctCourse());
                        break;
                    case "From csv file":
                        StudentsDataAccess.importFromCSV();
                        data = StudentsDataAccess.searchStudents();
                        showStudents(data);
                        comboBox.setItems(StudentsDataAccess.distinctCourse());
                        break;
                    default:
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    private void showStudents (ObservableList<Table> data) throws ClassNotFoundException {
        table.setItems(data);
    }
    @FXML
    void searchBy(MouseEvent event,String course) throws SQLException, ClassNotFoundException {
        ObservableList<Table> tab = FXCollections.observableArrayList();
        tab = StudentsDataAccess.searchCourse(course);
        showStudents(tab);
    }
  @FXML
  void submitPressed(ActionEvent event) throws SQLException, ClassNotFoundException {
      trueStatements = validation();
      if(trueStatements == 3)
          addNew(nameString,groupString,lastNameString);
  }
    int validation(){
        trueStatements = 0;
      if(nameField.getText().matches("[A-Za-z]+")){
          nameField.setStyle("-fx-background-color: #ad4747");
          nameString = nameField.getText();
          nameString = nameString.substring(0,1).toUpperCase() +nameString.substring(1);
          System.out.println(nameString);
          trueStatements++;
      }
      else{
          nameField.setStyle("-fx-background-color:#6e4141");
      }
      if(lastNameField.getText().matches("[A-Za-z]+")){
          lastNameField.setStyle("-fx-background-color: #ad4747");
          lastNameString = lastNameField.getText();
          lastNameString = lastNameString.substring(0,1).toUpperCase() + lastNameString.substring(1);
          System.out.println(lastNameString);
          trueStatements++;
      }
      else{
          lastNameField.setStyle("-fx-background-color:#6e4141");
      }
      if(groupField.getText().matches("[A-Za-z]+")){
          groupField.setStyle("-fx-background-color: #ad4747");
          groupString = groupField.getText();
          groupString = groupString.substring(0,1).toUpperCase() + groupString.substring(1);
          System.out.println(groupString);
          trueStatements++;
      }
      else{
          groupField.setStyle("-fx-background-color:#6e4141");
      }
      return trueStatements;
  }
    void addNew(String name, String course, String lastName) throws SQLException, ClassNotFoundException {
        StudentsDataAccess.insertStudent(course, name,lastName,"");
        data = StudentsDataAccess.searchStudents();
        comboBox.setItems(StudentsDataAccess.distinctCourse());
        showStudents(data);
    }
}
