/*
 * Jeff McGirr
 * mf-structure definition
 * */

import java.util.ArrayList;
import java.util.Iterator;

public class emfStruct {
    static class fNode {
        int gv; //GV ID#
        String op; // Operation
        String col; // column
    }

    static class gvSelNode {
        int gv; // GV id
        String col; // col to limit
        String op; // operator = > < <>
        String val; // value of the col
    }

    String wheres;

    public String[] selects;
    public int numGvs; // #GVs
    public String[] groupbys;
    public String groupbyStr;
    public String[] fvecNames;
    public ArrayList<fNode> fvec = new ArrayList<>();
    public ArrayList<gvSelNode> gvSel = new ArrayList<>();
    public String having = "";
    public String havingProg = ""; // changed to java syntax

//    print out the EMF struct for debug
    public void printEmf() {
        System.out.print("SELECTS: ");
        for (int i = 0; i<selects.length; i++) {
            if (i>0) {
                System.out.print(", ");
            }
            System.out.print(selects[i]);
        }
        System.out.println();
        System.out.println("# GROUPING VARS: " + numGvs);
        System.out.print("GROUP BY: ");
        for (int i = 0; i<groupbys.length; i++) {
            if (i>0) {
                System.out.print(", ");
            }
            System.out.print(groupbys[i]);
        }
        System.out.println();
        System.out.print("F-VECTOR: ");
        for (int i = 0; i<fvec.size(); i++) {
            if (i>0) {
                System.out.print(", ");
            }
            System.out.print(fvec.get(i).gv + "_" + fvec.get(i).op + "_" + fvec.get(i).col);
        }
        System.out.println();
        System.out.print("GV SELECTS: ");
        for (int i = 0; i<gvSel.size(); i++) {
            if (i>0) {
                System.out.print(", ");
            }
            System.out.print(gvSel.get(i).gv + "." + gvSel.get(i).col + "=" + gvSel.get(i).val);
        }
        System.out.println();
        if (having.length()>0){
            System.out.println("HAVING: " + having);
        }
    }
}
