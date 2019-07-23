# EMF Query Processor

### About
Set up for a table with the following schema: Sales(cust,prod,day,month,year,state,sale) in a Postgres DB  
Uses an input file containing an EMF query broken down into the specified syntax, to generate a new Java program that when run will generate results for that query  
Code by Jeff McGirr - written for a databases final project Spring 2019  
Based off 'Evaluation of Ad Hoc OLAP : In-Place Computation' by Damianos Chatziantoniou

### Generalizations/Enhancements  
Some improvements that could be made past the spec required for the class:  
-Dynamically getting schema information to allow use with any DB  
-Current execution is highly inefficient, reading the source table far more than needed. Currently, GV data for every row involves a table scan - these variable should instead be stored in memory and simply updated during scans.

### Basic Setup
Point all instances of dbConnect.java to the postgre DB you intend to connect to.

### Compiling the main program
Run ```javac Main.java```

### Generating query code
Using the compiled Main code, run ```java Main input/filename.txt``` (change filename)  
The path/filename argument must be in that format, only the directory and filename may change.  
Ex. 'folder/otherfile.txt' would be valid (assuming it exists), 'noPathOrExtFile' would not be.  
Compile using javac

### Running query code
Using the compiled query code, run ```java filename```  
(Must have Postgres driver set up in CLASSPATH, or call it manually via -cp)  
The compiled dbConnect file and the postgre JAR must be present in the same directory.  
These instructions are for Windows - the semicolon ; may need to be a colon : on other systems.

### Input Files
Must be exactly 11 or 12 lines, odd # lines are just headers, even are as follows:  
2: Select attributes (1 or more comma separated strings 'colname' or 'gv#_aggrFn_col')  
4: # of Grouping Vars (1 integer)  
6: Group By columns (1 or more comma separated strings 'oolname')  
8: F-Vect - all functions that need be computed - GV# of 0 is the  (1 or more comma separated strings 'gv#_aggrFn_col')  
10: GV Select Conditions (1 or more comma separated strings 'GV#.colname='val') - GV0 is for the query itself (WHERE, can only be and)  
12: Having condition - can be blank (1 or more string separated by 'and' or 'or' 'gv#_aggrFn_col [operator] [val]' - [val] can be another 'gv#_aggrFn_col', a plain value, or a combination)  