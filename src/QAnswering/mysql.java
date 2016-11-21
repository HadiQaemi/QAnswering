/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package qclassifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author DFL-H
 */
public class mysql {
     public static void main(String[] argv) throws FileNotFoundException, IOException, SQLException {
        PreparedStatement preparedStatement = null;
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
	} catch (ClassNotFoundException e) {
            System.out.println(e);
            return;
	} 
	try {
            connection = DriverManager
            .getConnection("jdbc:mysql://localhost:3306/qc","root", "");
	} catch (SQLException e) {
            System.out.println(e);
            return;
	}
        Statement selectStmt = connection.createStatement();
        ResultSet rs = (ResultSet) selectStmt
        .executeQuery("SELECT count(*) FROM `unigrams` ");
        //selectStmt = connection.createStatement();
        //ResultSet rs = selectStmt.executeQuery("SELECT ID,FIRST_NAME,LAST_NAME,STAT_CD FROM EMPLOYEE WHERE ID <= 10");
        rs.next();
        String st = rs.getString(1);
        if(Integer.parseInt(st)==0){
            preparedStatement = connection.prepareStatement("INSERT INTO `unigrams` (`value`) VALUES (?)");
            preparedStatement.setString(1, "Test");
            System.out.println("ss");
        }else{
            preparedStatement = connection.prepareStatement("INSERT INTO `sentences` (`sentence`) VALUES (?)");
            preparedStatement.setString(1, "Test");
        }
        System.out.println(st);    //First Column
        
	
  }
}
