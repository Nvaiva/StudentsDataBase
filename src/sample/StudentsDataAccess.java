package sample;

import com.itextpdf.text.*;

import java.io.*;

import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Iterator;
import java.util.StringTokenizer;

import javafx.collections.ObservableSet;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import static javax.print.attribute.standard.ReferenceUriSchemesSupported.FILE;


public class StudentsDataAccess {
    public static ObservableList<Table> searchCourse(String course) throws SQLException, ClassNotFoundException {
        ObservableList<Table> studentList = FXCollections.observableArrayList();
        String selectStmt = "SELECT * FROM student WHERE Course='" + course + "'";
        try {
            //Get ResultSet from dbExecuteQuery method
            ResultSet rsTable = DBUtils.dbExecuteQuery(selectStmt);
            //Send ResultSet to the getEmployeeFromResultSet method and get employee object
            while (rsTable.next()) {
                studentList.add(getStudentFromResultSet(rsTable));
            }
            return studentList;
        } catch (SQLException e) {
            System.out.println("While searching a student with " + course + " id, an error occurred: " + e);
            //Return exception
            throw e;
        }
    }
    public static ObservableList<String> distinctCourse() throws SQLException, ClassNotFoundException {
        ObservableList<String> courseList = FXCollections.observableArrayList();
        String selectStmt = "SELECT distinct Course FROM student";
        try {
            //Get ResultSet from dbExecuteQuery method
            ResultSet rsTable = DBUtils.dbExecuteQuery(selectStmt);
            //Send ResultSet to the getEmployeeFromResultSet method and get employee object
            while (rsTable.next()) {
                courseList.add(rsTable.getString("Course"));
            }
            courseList.add("All courses");
            return courseList;
        } catch (SQLException e) {
            System.out.println("While searching an student with " +  " id, an error occurred: " + e);
            //Return exception
            throw e;
        }
    }
    private static Table getStudentFromResultSet(ResultSet rs) throws SQLException {
        Table tab = null;
        if (rs != null) {
            tab = new Table(rs.getInt("ID"),rs.getString("Course"), rs.getString("Name"),
                    rs.getString("LastName"), rs.getString("Email"));
        }
        return tab;
    }

    public static ObservableList<Table> searchStudents() throws SQLException, ClassNotFoundException {
        //Declare a SELECT statement
        String selectStmt = "SELECT * FROM student";
        //Execute SELECT statement
        try {
            //Get ResultSet from dbExecuteQuery method
            ResultSet rsTable = DBUtils.dbExecuteQuery(selectStmt);

            //Send ResultSet to the getEmployeeList method and get employee object
            ObservableList<Table> studentList = getStudentList(rsTable);
            //Return employee object
            return studentList;
        } catch (SQLException e) {
            System.out.println("SQL select operation has been failed: " + e);
            //Return exception
            throw e;
        }
    }

    //Select * from employees operation
    private static ObservableList<Table> getStudentList(ResultSet rs) throws SQLException, ClassNotFoundException {
        //Declare a observable List which comprises of Employee objects
        ObservableList<Table> studentList = FXCollections.observableArrayList();
        while (rs.next()) {
            Table tab = new Table(rs.getInt("ID"),rs.getString("Course"), rs.getString("Name"), rs.getString("LastName"), rs.getString("Email"));
            studentList.add(tab);
        }
        //return empList (ObservableList of Employees)
        return studentList;
    }

    public static void deleteStudentWithId(int studentId) throws SQLException, ClassNotFoundException {
        //Declare a DELETE statement
        String updateStmt = "DELETE FROM student where ID = '" + studentId + "'";
        //"INSERT INTO student (Course,Name,LastName,Email) VALUES('" +course+ "','" + name + "','" + lastname + "','" + email + "')";
        //Execute UPDATE operation
        try {
            DBUtils.dbExecuteUpdate(updateStmt);
        } catch (SQLException e) {
            System.out.print("Error occurred while DELETE Operation: " + e);
            throw e;
        }
    }
    public static void updateStudent(int studentId, String course, String name, String lastName) throws SQLException, ClassNotFoundException {
        DBUtils.dbConnect();
        String query = "Update student set Course = ?, Name = ?, LastName = ? where ID = ?";
        PreparedStatement preStmt = DBUtils.getConn().prepareStatement(query);
        preStmt.setString(1,course);
        preStmt.setString(2,name);
        preStmt.setString(3,lastName);
        preStmt.setInt(4,studentId);
        preStmt.executeUpdate();
    }

    public static void insertStudent(String course, String name, String lastname, String email) throws SQLException, ClassNotFoundException {
        //Declare a DELETE statement
        int nullID = 0;
        String updateStmt = "INSERT INTO student (ID,Course,Name,LastName,Email) VALUES('" + nullID + "','" + course + "','" + name + "','" + lastname + "','" + email + "')";
        try {
            DBUtils.dbExecuteUpdate(updateStmt);
        } catch (SQLException e) {
            System.out.print("Error occurred while DELETE Operation: " + e);
            throw e;
        }
    }

    public static void exportToExcel() throws SQLException, ClassNotFoundException, IOException {
        String updateStmt = "SELECT * FROM student";
        ResultSet rs = DBUtils.dbExecuteQuery(updateStmt);
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("StudentsData.xls");
        HSSFRow header = sheet.createRow(0);

        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Course");
        header.createCell(2).setCellValue("Name");
        header.createCell(3).setCellValue("Last Name");
        header.createCell(4).setCellValue("Email");

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        sheet.autoSizeColumn(3);
        sheet.autoSizeColumn(4);

        int index = 1;
        while (rs.next()) {
            HSSFRow row = sheet.createRow(index);
            row.createCell(0).setCellValue(rs.getInt("ID"));
            row.createCell(1).setCellValue(rs.getString("Course"));
            row.createCell(2).setCellValue(rs.getString("Name"));
            row.createCell(3).setCellValue(rs.getString("LastName"));
            row.createCell(4).setCellValue(rs.getString("Email"));
            index++;
        }
        FileOutputStream fileOut = new FileOutputStream("StudentsData.xls");
        wb.write(fileOut);
        fileOut.close();
    }

    public static void exportToCSV() throws IOException, SQLException, ClassNotFoundException {
        FileWriter fw = new FileWriter("C:\\ProgramData\\MySQL\\MySQL Server 8.0\\Data\\students\\StudentsData.csv");
        String updateStmt = "SELECT * FROM student";
        ResultSet rs = DBUtils.dbExecuteQuery(updateStmt);
        int cols = rs.getMetaData().getColumnCount();
        for (int i = 1; i <= cols; i++) {
            fw.append(rs.getMetaData().getColumnLabel(i));
            if (i < cols) fw.append(',');
            else fw.append('\n');
        }
        while (rs.next()) {
            for (int i = 1; i <= cols; i++) {
                fw.append(rs.getString(i));
                if (i < cols) fw.append(',');
            }
            fw.append('\n');
        }
        fw.flush();
        fw.close();
    }

    public static void exportToPDF() throws FileNotFoundException, SQLException, ClassNotFoundException {
        String fileName = "StudentsData.pdf";
        Document document = new Document(PageSize.A4);
        String updateStmt = "SELECT * FROM student";
        ResultSet rs = DBUtils.dbExecuteQuery(updateStmt);
        ResultSetMetaData rsMetaData = rs.getMetaData(); //number of columns and names
        int numberOfColumns = rsMetaData.getColumnCount();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(new File((fileName))));
            document.open();
            Paragraph title = new Paragraph("Students attendance");
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(32);
            document.add(title);
            PdfPTable table = new PdfPTable(numberOfColumns);
            PdfPCell table_cell;

            //column numbers starts from 1 !!!
            for (int i = 1; i <= numberOfColumns; i++){
                String column = rsMetaData.getColumnName(i);
                table_cell = new PdfPCell(new Phrase(column));
                table.addCell(table_cell);
            }
            while (rs.next()) {
                int ID = rs.getInt("ID");
                table_cell = new PdfPCell(new Phrase(Integer.toString(ID)));
                table.addCell(table_cell);
                String course = rs.getString("Course");
                table_cell = new PdfPCell(new Phrase(course));
                table.addCell(table_cell);
                String name = rs.getString("Name");
                table_cell=new PdfPCell(new Phrase(name));
                table.addCell(table_cell);
                String lastName = rs.getString("LastName");
                table_cell=new PdfPCell(new Phrase(lastName));
                table.addCell(table_cell);
                String email = rs.getString("Email");
                table_cell=new PdfPCell(new Phrase(email));
                table.addCell(table_cell);
            }
            document.add(table);
            document.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void importFromCSV() throws SQLException, ClassNotFoundException, IOException {
        String updateStmt = "Delete from student";
        DBUtils.dbExecuteUpdate(updateStmt);
        String updateStmt2 = "LOAD DATA INFILE 'StudentsData.csv'" +
                "INTO TABLE student FIELDS TERMINATED BY ','" +
                "ENCLOSED BY '' LINES TERMINATED BY '\n' IGNORE 1 ROWS";
        DBUtils.dbExecuteUpdate(updateStmt2);
    }

    public static void importFromExcel() throws IOException, SQLException, ClassNotFoundException {
        String updateStmt = "Delete from student";
        DBUtils.dbExecuteUpdate(updateStmt);
        DBUtils.dbConnect();
        try {
            DBUtils.getConn().setAutoCommit(false);
            PreparedStatement ps = null;
            FileInputStream input = new FileInputStream("StudentsData.xls");
            POIFSFileSystem fs = new POIFSFileSystem(input);
            Workbook workbook;
            workbook = WorkbookFactory.create(fs);
            Sheet sheet = workbook.getSheetAt(0);
            Row row;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                double id = row.getCell(0).getNumericCellValue();
                String course = row.getCell(1).getStringCellValue();
                String name = row.getCell(2).getStringCellValue();
                String lastName = row.getCell(3).getStringCellValue();
                String email = row.getCell(4).getStringCellValue();

                String updateStmt2 = "INSERT INTO student VALUES('"+0+"','" + course + "','" + name + "','" + lastName + "','" + email + "')";
                ps = DBUtils.getConn().prepareStatement(updateStmt2);
                ps.execute();
            }
            DBUtils.getConn().commit();
            ps.close();
            input.close();
            System.out.println("Success import excel to mysql table");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}

