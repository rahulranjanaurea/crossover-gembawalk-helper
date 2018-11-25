# Crossover-GembaWalk-Helper

### Description

This application exports Gemba Walk information to excel

### Prerequisites
1. JDK 8

### Required Common Application Properties
***`username:`*** That is spring properties. Below is the example properties.

***`password:`*** That is spring properties. Below is the example properties.

`--username=<youremail@domain.com> --password=<password>`

 Input will be project file. This generally we give for the project for which analyzation has to be done. This is base path

#Other Application Properties

***start.date:*** Passing the startdate. If you don't pass it, starting of current week will be start date.

`start date will be passed as --start.date=2018-11-20`

***end.date:*** Passing the enddate. If you don't pass it, ending of current week will be start date.

`end date will be passed as --end.date=2018-11-20`

`Date passed should always be in in yyyy-MM-dd i.e 2018-11-20`

### Running the application
Running the application with username and password

 `java -jar target/crossover-helper-0.0.1-SNAPSHOT.jar --username=<your email>  --password=<yourpassword>`
 
 Running the application with username and password and custom start date

`java -jar target/crossover-helper-0.0.1-SNAPSHOT.jar --username=<your email>  --password=<yourpassword> --start
.date=<your-date-like-2018-11-20>`

 Running the application with username and password and custom end date
 
`java -jar target/crossover-helper-0.0.1-SNAPSHOT.jar --username=<your email>  --password=<yourpassword> --end.date=<your-date-like-2018-11-20>`
 
  Running the application with username and password and custom start and end date
  
`java -jar target/crossover-helper-0.0.1-SNAPSHOT.jar --username=<your email>  --password=<yourpassword> --start.date=<your-date-like-2018-11-20> 
   --end.date=<your-date-like-2018-11-20>`
   
### Face Detection
This program has face detection mechanism, which works on haar-cascade. Since it will be bit slow, So its alway advisable to put at most 
week long data. By default it is disabled. 
To enable it append 

`--enable.face.detection=true`

to command line.

Since face detection downloads the file from url, and url has expiry. So sometimes, when programs run longer, its url will get expired. 
  
###Notes:
Always try to replace placeholders with real values. I tried to mark placeholder startind with < and ending with >.

When ever you are passing the values from command line, special character should be escaped.
Like if you have password like abc#123. Since # in spacial character for bash, you should pass password as abc\#123.

Since microsoft puts a lock on file when open for writing, Please make sure you close the excel when running the program. Else file will 
not be written.
https://stackoverflow.com/questions/33417640/excel-file-cant-be-accessed-by-java-program-if-excel-file-is-open

