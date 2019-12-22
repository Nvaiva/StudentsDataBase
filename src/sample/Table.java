package sample;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.Serializable;

public class Table implements Serializable {
    private SimpleIntegerProperty ID;
    private SimpleStringProperty group;
    private SimpleStringProperty name;
    private SimpleStringProperty lastName;
    private SimpleStringProperty attendance;
    public Table(int ID, String group, String name, String lastName, String attendance){
        this.ID = new SimpleIntegerProperty(ID);
        this.group = new SimpleStringProperty(group);
        this.name = new SimpleStringProperty(name);
        this.lastName = new SimpleStringProperty(lastName);
        this.attendance = new SimpleStringProperty(attendance);
    }

    public String getGroup() {
        return group.get();
    }
    public String getName() {
        return name.get();
    }
    public int getID() {return ID.get();}
    public String getLastName() { return lastName.get(); }
    public String getAttendance() {return attendance.get(); }

    //public String setGroup(String group) {this.finalGroup = group;}
}