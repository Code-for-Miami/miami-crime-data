/**
dataparse.groovy
Written by Antonio Llanos (@allanos)

Revision History
-=-=-=-=-=-=-=-=-
1.0 - Initial Development

Input: a filename of single datapoint per line corresponding to the required format
Output: SQL statements to be used within the application

Currently requires "brute force" to complete, the program parses a Copy/Paste of the tabular data from a MDPD CAS Detail by Patrol Area report.
This creates a cell per line file when pasted into an editor.  The program expects the following data:

Grid
Quad
Agency Report Number
Incident Date Time
Incident To Date Time
Address
Business Name
Signal
Classification Type
Clear Type
Case Type
Det Badge
M.O. Description
M.O. Remark

If the data isn't in this exact format, it will error out, most likely due to parsing error.  In the event the source is missing data a blank line 
should be entered.

There are 2 translations required to convert MDPD codes to internal ids.  Signals represent types of crimes, and classifications represent classes
crime.  These currently are hard coded to match a table in the system, these should be moved to a lookup table and an error should be thrown to 
indicate a new decode is required.
**/
def f = new File(args[0])
def signals = ['22A' : 1,'22S':2,'26C':3,'26R':4,'26V':5,'27O':6,'29':7,'31':8,'32A':9,'32B':10,'33':11,'33A':12,'33E':13,'33L':14]
def classification = ['VEHICLE':1,'COMMERCIAL':2,'OVER':3,'RESIDENTIAL':4,'':5,'FROM INTERIOR OF VEHICLE':6]
def i = 0
def sql = "INSERT INTO `crime_reports`.`incident` (`version`,`district_id`,`grid`,`quad`,`agency_report_number`,`incident_date_time`,`incident_to_date_time`,`address`,`business_name`,`crime_signal_id`,`classification_id`,`clear_type`,`case_type`,`badge_number`,`mo_description`,`mo_remarks`) VALUES ("
def buf = new StringBuffer(sql);
def sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm");
def msqlDf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
def std = [1,2,5,6,10,9,11,12]

f.eachLine { ln ->

   if (i == 0) { buf.append("0,1,") 
    buf.append("'${ln}',") }
   if (std.contains(i)) { buf.append("'${ln}',") }
   if (i ==7) { 

      buf.append("${signals.get(ln.split("-").first().trim())},")
   }
   if (i == 8)
    {
    	
    	buf.append(classification.get(ln))
    	buf.append(",")
    }
   if (i == 3 || i == 4) 
     { 
       def dt = sdf.parse(ln) 
       buf.append("'${msqlDf.format(dt)}',")
     }
   if (i==13) { buf.append("'${ln}'")  }
   i++;
   if (i > 13) { 
   	 buf.append(");")
     println buf.toString()
     buf = new StringBuffer(sql)
   	 i = 0
   	}  
   
}
