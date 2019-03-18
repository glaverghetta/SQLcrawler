package usf.edu.bronie.sqlcrawler.tools;

import usf.edu.bronie.sqlcrawler.io.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RandomCodeSelector {

//    private static final String SQL = "select * from code_stats where sql_usage!=0 order by rand() limit 1";
    private static final String SQL = "select * from code_stats where sql_usage!=0 and url not like '%test%' order by rand() limit 1";
    private Connection mConnection = DBConnection.getConnection();
    private JLabel label1 = new JLabel();

    public void runSelector(){
        simpleButton();
    }

    public void simpleButton(){
        JFrame f=new JFrame("Button Example");
        //submit button
        JButton b=new JButton("Submit");
        b.setBounds(100,100,140, 40);
        //enter name label
        //empty label which will show event after button clicked
        label1.setBounds(10, 110, 200, 100);
        //textfield to enter name
        f.add(label1);
        f.add(b);
        f.setSize(300,300);
        f.setLayout(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //action listener
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    printResult();
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printResult() throws SQLException, MalformedURLException {
        Statement stmt = mConnection.createStatement();
        ResultSet resultSet = stmt.executeQuery(SQL);
        resultSet.next();
        int usage = resultSet.getInt("sql_usage");
        String url = resultSet.getString("url");
        URL u = new URL(url);
        openWebpage(u);
        printSQLUsage(usage);
        resultSet.close();
    }

    private void printSQLUsage(int usage) {
        String str = "Usage: ";
        switch (usage) {
            case 0: str += "None"; break;
            case 1: str += "hardcoded"; break;
            case 2: str += "concat"; break;
            case 3: str += "param + concat"; break;
            case 4: str += "param"; break;
            default:break;
        }

        label1.setText(str);
    }

    private static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}
