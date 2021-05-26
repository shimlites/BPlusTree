Task 1: Implement Heap File in Java 
Set up a git repository for code, and complete the following programming tasks using Java on the AWS linux instance. 
4.0.1 Explain your design
•	This should include justification for using fixed length vs variable length fields and how they were implemented (eg delimiters, headers etc.)
•	Discuss alternatives and their advantages and disadvantages.
A program to load a database relation writing a heap file
The source records are variable-length. Your heap file may hold fixed-length records (you will need to choose appropriate maximum lengths for each field). However, you may choose to implement variable lengths for some fields, especially if you run out of disc space or secondary memory! 
All attributes with Int type must be stored in 4 bytes of binary, e.g. if the value of ID is equal to 70, it must be stored as 70 (in decimal) or 46 (in hexadecimal; in Java: 0x46). It must not be stored as the string “70”, occupying two bytes. Your heap file is therefore a binary file.
 For simplicity, the heap file does not need a header (containing things like the number of records in the file or a free space list), though you might need to keep a count of records in each page. The file should be packed, i.e. there is no gap between records, but there will need to be gaps at the end of each page. 

The executable name of your program to build a heap file must be dbload and should be executed using the command:
java dbload -p pagesize datafile 
The output file will be heap.pagesizewhere your converted binary data is written as a heap.
Your program should write out one “page” of the file at a time. For example, with a pagesizeof 4096, you would write out a page of 4096 bytes possibly containing multiple records of data to disk at a time. You are not required to implement spanning of records across multiple pages.
 Your dbload program must also output the following to stdout, the number of records loaded, number of pages used and the number of milliseconds to create the heap file. 
You are also suggested the use of utilities like xxd for examining the output heap file to see if their code is producing the expected format. ie
xxdheap.pagesize | less 

A program that performs a text search using your heap file
Write a program to perform text query search operations on the field “SDT NAME” heap file (without an index) produced by your dbloadprogram. Note that SDT NAME is a new field you created by considering Sensor ID and Date Time as strings and connecting them together. 
The executable name of your program to build a heap file must be dbqueryand should be executed using the command: 
java dbquery text pagesize 3
Your program should read in the file, one “page” at a time. For example, if the pagesizeparameter is 4096, your program should read in the records in the first page in heap.4096 from disk. These can then be scanned, in-memory, for a match (the string in text parameter is contained in the field “SDT NAME”). If a match is found, print the matching record to stdout, there may be multiple answers. Then read in the next page of records from the file. The process should continue until there are no more records in the file to process.
 In addition, the program must always output the total time taken to do all the search operations in milliseconds to stdout

Task 2: Implement a B+-tree Index in Java
Implement aB+-tree Index in Javafor you heap file from Assignment 1 and conduct experiments querying (equality query and range query) with and without the index.

Data
The data that you are going to use in this assignment is open data from the City of Melbourne about pedestrian traffic in the Melbourne CBD (download from https://data.melbourne. vic.gov.au/Transport/Pedestrian-Counting-System-Monthly-counts-per-hour/ b2ak-trbp). please refer to the help file available that describes the data set from the provided link.

Important:
•	Your program may be developed on any machine, but must compile and run your AWS linux instance. In particular your code must comply with the java 1.8 standard as that's the version of java that we have gotten you to install there. 
•	You must use git as you develop your code (wherever you do the development). As you work on the assignment you should commit your changes to git regularly (for example, hourly or each time you rebuild) as the log may be used as evidence of your progress. 
•	Paths must not be hard-coded. That is, the program should not require the input files to be in a specific directory - it may load the data from any directory and your program should work correctly. 
•	Diagnostic messages must be output to stderr.
