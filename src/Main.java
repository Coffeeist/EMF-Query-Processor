/*
 * Jeff McGirr
 * Main File
 * This code generates Java code for input ESQL data. The generated Java code connects to a PostgreSQL database for initial data load
 * And then on it's own runs through the data to generate a report.
 * */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;

public class Main {
    private static dbConnect db = new dbConnect();
    private static String filename = "generated";

    // dirty check for ints helper
    private static boolean isNum(String s) {
        try {
            Integer.parseInt(s);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private static void writeFile(emfStruct emf) {
        try {
            FileWriter fw = new FileWriter("out/production/FP/generated/" + filename + ".java");
            PrintWriter pw = new PrintWriter(fw);
            String sep = System.lineSeparator();
            pw.print("/*" + sep +
                    "* Jeff McGirr" + sep +
                    "* Generated Query Code for " + filename + sep +
                    "* */" + sep +
                    "" + sep +
                    "import java.sql.ResultSet;" + sep +
                    "import java.util.ArrayList;" + sep +
                    ""+ sep +
                    "public class " + filename + " {" + sep +
                    "    private static dbConnect db = new dbConnect();" + sep);
            pw.print("    " + sep +
                    "private static boolean isNum(String s) {" + sep +
                    "        try {" + sep +
                    "            Integer.parseInt(s);" + sep +
                    "        } catch (Exception e) {" + sep +
                    "            return false;" + sep +
                    "        }" + sep +
                    "        return true;" + sep +
                    "}" + sep +
                    "" + sep);
            pw.print("" + sep +
                    "    public static void main(String[] args) {" + sep);
            if (emf.wheres.length()>0) {
                pw.print("        ResultSet rs = db.getRs(\"" + emf.wheres + "\");" + sep);
                pw.print("        ResultSet rsRows = db.getGroups(\"" + emf.wheres + "\",\"" + emf.groupbyStr + "\");" + sep);
                // I used the DB again to get rows as Java lacks convenient tools for cartesian product as needed to handle multiple groupbys
            } else {
                pw.print("        ResultSet rs = db.getRs();" + sep);
                pw.print("        ResultSet rsRows = db.getGroups(\"" + emf.groupbyStr + "\");" + sep);
            }
            String printColArr = "{";
            for (int i = 0; i < emf.selects.length; i++) {
                if (i>0) {
                    printColArr = printColArr + ",";
                }
                printColArr = printColArr + "\"" + emf.selects[i] + "\"";
            }
            printColArr = printColArr + "}";
            pw.print("        String[] colNames = " + printColArr + ";" + sep);
            // create an array of arrays (table) for data - essentially the mf-structure
            pw.print("        ArrayList<ArrayList<String>> dataTable = new ArrayList<>();" + sep);
            pw.print("        try {" + sep);
            pw.print("            int rowcols = rsRows.getMetaData().getColumnCount();" + sep);
            pw.print("            int sum = 0;" + sep);
            pw.print("            int count = 0;" + sep);
            pw.print("            int min = 0;" + sep);
            pw.print("            int max = 0;" + sep);
            pw.print("            int locPtr = 0;" + sep);
            pw.print("            while(rsRows.next()) {" + sep +
                    "                ArrayList<String> newRow = new ArrayList<>();" + sep +
                    "                for (int i=1; i <= rowcols; i++) {" + sep +
                    "                    newRow.add(rsRows.getString(i));" + sep +
                    "                    locPtr = i;" + sep +
                    "                }" + sep);
            // now fetch data
            for (int i = 0; i < emf.fvec.size(); i++) {
                int curGv = emf.fvec.get(i).gv;
                String gvIfs = "";
                int gvIfsCtr = 0;
                ArrayList<String> tempCols = new ArrayList<>(Arrays.asList(emf.selects));
                for (int j = 0; j < emf.gvSel.size(); j++) {
                    if (emf.gvSel.get(j).gv == curGv) {
                        if (gvIfsCtr > 0) {
                            gvIfs = gvIfs + " && ";
                        }
                        if (tempCols.contains(emf.gvSel.get(j).val)) {
                            int loc = tempCols.indexOf(emf.gvSel.get(j).val);
                            switch (emf.gvSel.get(j).op) {
                                case "==":
                                    gvIfs = gvIfs + "rs.getString(\"" + emf.gvSel.get(j).col + "\").equals(newRow.get(" + loc + "))";
                                    break;
                                case "!=":
                                    gvIfs = gvIfs + "!(rs.getString(\"" + emf.gvSel.get(j).col + "\").equals(newRow.get(" + loc + ")))";
                                    break;
                                default:
                                    gvIfs = gvIfs + "rs.getInt(\"" + emf.gvSel.get(j).col + "\") " + emf.gvSel.get(j).op + " Integer.parseInt(newRow.get(" + loc + "))";
                                    break;
                            }
                        } else {
                            switch (emf.gvSel.get(j).op) {
                                case "==":
                                    gvIfs = gvIfs + "rs.getString(\"" + emf.gvSel.get(j).col + "\").equals(\"" + emf.gvSel.get(j).val + "\")";
                                    break;
                                case "!=":
                                    gvIfs = gvIfs + "!(rs.getString(\"" + emf.gvSel.get(j).col + "\").equals(\"" + emf.gvSel.get(j).val + "\"))";
                                    break;
                                default:
                                    gvIfs = gvIfs + "rs.getString(\"" + emf.gvSel.get(j).col + "\") " + emf.gvSel.get(j).op + " " + emf.gvSel.get(j).val + "";
                                    break;
                            }
                        }
                        gvIfsCtr++;
                    }
                }
                if (curGv == 0) {
                    gvIfs = "";
                    for (int j = 0; j < emf.groupbys.length; j++) {
                        if (j > 0) {
                            gvIfs = gvIfs + " && ";
                        }
                        int loc = tempCols.indexOf(emf.groupbys[j]);
                        gvIfs = gvIfs + "rs.getString(\"" + emf.groupbys[j] + "\").equals(newRow.get(" + loc + "))";
                    }
                }
                pw.print("                sum = 0;" + sep);
                pw.print("                count = 0;" + sep);
                switch (emf.fvec.get(i).op) {
                    case "avg":
                        pw.print("                while(rs.next()) {" + sep +
                                "                    if (" + gvIfs + ") {" + sep +
                                "                        count++;" + sep +
                                "                        sum = sum + rs.getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                    }" + sep +
                                "                }" + sep +
                                "                rs.beforeFirst();" + sep +
                                "                int _" + emf.fvecNames[i] + " = 0;" + sep +
                                "                if(count > 0) {" + sep +
                                "                    _" + emf.fvecNames[i] + " = (sum/count);" + sep +
                                "                }" + sep +
                                "                // System.out.println(_" + emf.fvecNames[i] + ");" + sep);
                        break;
                    case "count":
                        pw.print("                while(rs.next()) {" + sep +
                                "                    if (" + gvIfs + ") {" + sep +
                                "                        count++;" + sep +
                                "                    }" + sep +
                                "                }" + sep +
                                "                rs.beforeFirst();" + sep +
                                "                int _" + emf.fvecNames[i] + " = count;" + sep);
                        break;
                    case "min":
                        pw.print("                min = rs.first().getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                while(rs.next()) {" + sep +
                                "                    if (" + gvIfs + ") {" + sep +
                                "                        int curval = rs.getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                        if (curval<min) {" + sep +
                                "                            min = curval;" + sep +
                                "                        }" + sep +
                                "                    }" + sep +
                                "                }" + sep +
                                "                rs.beforeFirst();" + sep +
                                "                int _" + emf.fvecNames[i] + " = min;" + sep);
                        break;
                    case "max":
                        pw.print("                max = rs.first().getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                while(rs.next()) {" + sep +
                                "                    if (" + gvIfs + ") {" + sep +
                                "                        int curval = rs.getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                        if (curval>max) {" + sep +
                                "                            max = curval;" + sep +
                                "                        }" + sep +
                                "                    }" + sep +
                                "                }" + sep +
                                "                rs.beforeFirst();" + sep +
                                "                int _" + emf.fvecNames[i] + " = max;" + sep);
                        break;
                    case "sum":
                        pw.print("                while(rs.next()) {" + sep +
                                "                    if (" + gvIfs + ") {" + sep +
                                "                        sum = sum + rs.getInt(\"" + emf.fvec.get(i).col + "\");" + sep +
                                "                    }" + sep +
                                "                }" + sep +
                                "                rs.beforeFirst();" + sep +
                                "                int _" + emf.fvecNames[i] + " = sum;" + sep);
                        break;
                }
            }
            if (emf.having.length()>0) {
                pw.print("                if (" + emf.havingProg + ") {" + sep +
                        "                    for (int j = locPtr; j<colNames.length; j++) {" + sep);
                for (int i = emf.groupbys.length; i < emf.selects.length; i++) { // workaround due to lack of dynamic naming vars :|
                    pw.print("                        if (\"" + emf.selects[i] + "\" == colNames[j]) {" + sep +
                            "                            newRow.add(Integer.toString(_" + emf.selects[i] + "));" + sep +
                            "                        }" + sep);
                }
                pw.print("                   }" + sep +
                        "                } else {" + sep +
                        "                    for (int j = locPtr; j<colNames.length; j++) {" + sep +
                        "                        newRow.add(\"\");" + sep +
                        "                    }" + sep +
                        "                }" + sep);
            } else {
                pw.print("                for (int j = locPtr; j<colNames.length; j++) {" + sep);
                for (int i = emf.groupbys.length; i < emf.selects.length; i++) { // workaround due to lack of dynamic naming vars :|
                    pw.print("                        if (\"" + emf.selects[i] + "\" == colNames[j]) {" + sep +
                            "                            newRow.add(Integer.toString(_" + emf.selects[i] + "));" + sep +
                            "                        }" + sep);
                }
                pw.print("                   }" + sep);
            }
            pw.print("                dataTable.add(newRow);" + sep +
                    "            }" + sep);
            pw.print("        } catch (Exception e) {" + sep +
                            "            e.printStackTrace();" + sep +
                            "            System.err.println(e.getClass().getName()+\": \"+e.getMessage());" + sep +
                            "            System.exit(0);" + sep +
                            "        }" + sep);
            pw.print("//--------------------------------------------------" + sep +
                    "//----- PRINTING -----" + sep +
                    "//--------------------------------------------------" + sep +
                    "        try {" + sep +
                    "            for (int i=0; i < " + emf.selects.length + "; i++) {" + sep +
                    "                System.out.printf(\"| %-25s |\", colNames[i]);" + sep +
                    "            }" + sep + 
                    "            System.out.println(\"\");" + sep + 
                    "            for (int i=0; i < " + emf.selects.length + "; i++) {" + sep +
                    "                System.out.printf(\"| %-25s |\",\"\");" + sep + 
                    "            }" + sep + 
                    "            System.out.println(\"\");" + sep +
                    "            for (int i=0; i < dataTable.size(); i++) {" + sep +
                    "                for (int j=0; j < dataTable.get(i).size(); j++) {" + sep +
                    "                    if (isNum(dataTable.get(i).get(j))) {" + sep +
                    "                        System.out.printf(\"| %25s |\",dataTable.get(i).get(j));" + sep +
                    "                    } else {" + sep +
                    "                        System.out.printf(\"| %-25s |\",dataTable.get(i).get(j));" + sep +
                    "                    }" + sep +
                    "                }" + sep +
                    "                System.out.println(\"\");" + sep +
                    "            }" + sep +
                    "            System.out.println(\"\");" + sep +
                    "        } catch (Exception e) {" + sep + 
                    "            e.printStackTrace();" + sep + 
                    "            System.err.println(e.getClass().getName()+\": \"+e.getMessage());" + sep + 
                    "            System.exit(0);" + sep + 
                    "        }" + sep + 
                    "" + sep + 
                    "        db.closeConn();" + sep +
                    "   }" + sep +
                    "}");
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }

    private static void writeDebug(emfStruct emf) { // just straight up print cols from the DB for testing
        try {
            FileWriter fw = new FileWriter("out/production/FP/generated/" + filename + "DBG.java");
            PrintWriter pw = new PrintWriter(fw);
            String sep = System.lineSeparator();
            pw.print("/*" + sep +
                    "* Jeff McGirr" + sep +
                    "* DEBUG Generated Query Code for " + filename + sep +
                    "* */" + sep +
                    "" + sep +
                    "import java.sql.ResultSet;" + sep +
                    "import java.util.ArrayList;" + sep +
                    ""+ sep +
                    "public class " + filename + "DBG {" + sep +
                    "    private static dbConnect db = new dbConnect();" + sep +
                    "" + sep +
                    "    public static void main(String[] args) {" + sep);
            if (emf.wheres.length()>0) {
                pw.print("        ResultSet rs = db.getRs(\"" + emf.wheres + "\");" + sep);
            } else {
                pw.print("        ResultSet rs = db.getRs();" + sep);
            }
            pw.print("" + sep +
                    "        try {" + sep +
                    "            int cols = rs.getMetaData().getColumnCount();" + sep +
                    "            for (int i=1; i <= cols; i++) {" + sep +
                    "                String header = rs.getMetaData().getColumnName(i) + \" (\" + rs.getMetaData().getColumnClassName(i) + \")\";" + sep +
                    "                System.out.printf(\"| %-25s |\",header);" + sep +
                    "            }" + sep +
                    "            System.out.println(\"\");" + sep +
                    "            for (int i=1; i <= cols; i++) {" + sep +
                    "                System.out.printf(\"| %-25s |\",\"\");" + sep +
                    "            }" + sep +
                    "            System.out.println(\"\");" + sep +
                    "            while (rs.next()) {" + sep +
                    "                for (int i=1; i <= cols; i++) {" + sep +
                    "                    if (rs.getMetaData().getColumnClassName(i).equals(\"java.lang.Integer\")) {" + sep +
                    "                        System.out.printf(\"| %25s |\", rs.getString(i));" + sep +
                    "                    } else {" + sep +
                    "                        System.out.printf(\"| %-25s |\", rs.getString(i));" + sep +
                    "                    }" + sep +
                    "                }" + sep +
                    "                System.out.println(\"\");" + sep +
                    "            }" + sep +
                    "        } catch (Exception e) {" + sep +
                    "            e.printStackTrace();" + sep +
                    "            System.err.println(e.getClass().getName()+\": \"+e.getMessage());" + sep +
                    "            System.exit(0);" + sep +
                    "        }" + sep +
                    "" + sep +
                    "        db.closeConn();" + sep +
                    "   }" + sep +
                    "}");
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            String inputName = args[0];
            inputName = inputName.split("\\.")[0];
            String[] inputNames = inputName.split("/");
            filename = inputNames[inputNames.length-1];
//            System.out.println(filename);
        }

        String lnS;

        emfStruct emf = new emfStruct();

        try {
            BufferedReader br = new BufferedReader(new FileReader(args[0]));
            String line;
            int lnCtr = 0;
            while ((line = br.readLine()) != null) {
                String temp = line;
                if (lnCtr != 11) {
                    temp = line.replaceAll("\\s+",""); // use a temp var to work with, and remove all spaces, unless having line
                }
                switch (lnCtr) {
                    // ignore odd lines (even in code), they're just headers
                    case 1:
                        emf.selects = temp.split(",");
                        break;
                    case 3:
                        emf.numGvs = Integer.parseInt(temp);
                        break;
                    case 5:
                        emf.groupbyStr = temp;
                        emf.groupbys = temp.split(",");
                        break;
                    case 7:
                        String[] fStmts = temp.split(",");
                        emf.fvecNames = fStmts;
                        for (int i = 0; i<fStmts.length;i++) {
                            String[] fStmt = fStmts[i].split("_");
                            emfStruct.fNode newFN = new emfStruct.fNode();
                            newFN.gv = Integer.parseInt(fStmt[0]);
                            newFN.op = fStmt[1];
                            newFN.col = fStmt[2];

                            if (newFN.gv == 0) {
                                emfStruct.gvSelNode newSN = new emfStruct.gvSelNode();
                                newSN.op = "==";
                                newSN.gv = 0;
                                newSN.col = newFN.col;
                                newSN.val = newFN.col;
                                emf.gvSel.add(newSN);
                            }

                            emf.fvec.add(newFN);
                        }
                        break;
                    case 9:
                        String[] selStmts = temp.split(",");
                        String wheres = "";
                        for (int i = 0; i<selStmts.length;i++) {
                            String[] selStmt = selStmts[i].split("\\.");
                            emfStruct.gvSelNode newSN = new emfStruct.gvSelNode();
                            if (Integer.parseInt(selStmt[0]) == 0) {
                                // we're going to filter WHEREs (GV0) from the get go
                                if (wheres.length() > 0) {
                                    wheres = wheres + " AND ";
                                }
                                wheres = wheres + selStmt[1];
                            } else {
                                newSN.gv = Integer.parseInt(selStmt[0]);
                                if (selStmt[1].split("<>").length > 1) {
                                    selStmt = selStmt[1].split("<>");
                                    newSN.op = "!=";
                                } else if (selStmt[1].split(">=").length > 1) {
                                    selStmt = selStmt[1].split(">=");
                                    newSN.op = ">=";
                                } else if (selStmt[1].split("<=").length > 1) {
                                    selStmt = selStmt[1].split("<=");
                                    newSN.op = "<=";
                                } else if (selStmt[1].split(">").length > 1) {
                                    selStmt = selStmt[1].split(">");
                                    newSN.op = ">";
                                } else if (selStmt[1].split("<").length > 1) {
                                    selStmt = selStmt[1].split("<");
                                    newSN.op = "<";
                                } else if (selStmt[1].split("=").length > 1) {
                                    selStmt = selStmt[1].split("=");
                                    newSN.op = "==";
                                }
                                newSN.col = selStmt[0];
                                newSN.val = selStmt[1].replaceAll("'","");

                                emf.gvSel.add(newSN);
                            }
                        }
                        emf.wheres = wheres;
                        break;
                    case 11:
                        emf.having = temp;
                        temp = temp.replaceAll("<>","!=").replaceAll("and","&&").replaceAll("or","||").replaceAll("not","!").replaceAll("=","==");
                        temp = temp.replaceAll("(\\d+)_","_$1_"); // can't start vars with numbers ok thanks java thats nice...
                        emf.havingProg = temp;
//                        System.out.println(temp);
                        break;
                }

                lnCtr++;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

//        emf.printEmf(); //debug


        writeFile(emf);

//        writeDebug(emf);
    }
}
