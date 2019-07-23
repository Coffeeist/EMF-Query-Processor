/*
* Jeff McGirr
* Database Connection and Data Retriever
* */

import javax.xml.transform.Result;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class dbConnect {
    private Connection c = null;
    private Statement stmt = null;
    private ResultSet rs = null;

    public ResultSet getRs() {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "password");

//            System.out.println("Opened database successfully");

            stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * " +
                    "FROM sales ";
            rs = stmt.executeQuery(sql);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        return rs;
    }

    public ResultSet getRs(String wheres) {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "password");

//            System.out.println("Opened database successfully");

            stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT * " +
                    "FROM sales " +
                    "WHERE " + wheres;
            rs = stmt.executeQuery(sql);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        return rs;
    }

    public ResultSet getGroups(String groups) {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "password");

//            System.out.println("Opened database successfully");

            stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT " + groups + " " +
                    "FROM sales " +
                    "GROUP BY " + groups;
            rs = stmt.executeQuery(sql);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        return rs;
    }

    public ResultSet getGroups(String wheres, String groups) {
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "password");

//            System.out.println("Opened database successfully");

            stmt = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            String sql = "SELECT " + groups + " " +
                    "FROM sales " +
                    "WHERE " + wheres + " " +
                    "GROUP BY " + groups;
            rs = stmt.executeQuery(sql);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        return rs;
    }

    public void closeConn() {
        try {
            stmt.close();
            c.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }
}
