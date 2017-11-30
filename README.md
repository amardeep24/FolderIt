# FolderIt
Organizes loose files in a directory according to file type or according to the names given in the cmd line options.

## Prerequisites for building from source
1. Java JDK 1.8
2. Maven 3 or above.

## Prerequisites for running application
1. Java JRE 1.8

## Steps to build & run application

1. git clone https://github.com/amardeep24/FolderIt.git
2. cd FolderIt
3. mvn clean compile assembly:single
4. cd target
5. copy Folderit-x.x.x.jar from target and place it into desired directory.
5. java -jar Folderit-x.x.x.jar

## Version 1.0.0

1. Grouping based on extensions.
2. Grouping based on keywords provided.
3. Zip the grouped files into an archive.

## Version 1.1.0

1. Aliasing added.
2. Param switch prefixed with a hyphen.

## Application usage v1.0.0

1. java -jar Folderit-x.x.x.jar p [keyword1,keyword2...] zip
The "p" option indicates cmd line options are activated, keywords are the options which matches with the names of the files in 
the directory where this jar is executed. If a matching file name containing the keyword specified is found then a directory is
created having the same name. The files which matched with the keyword are moved into the newly created directory.The "zip" is 
a optional parameter if provided then a zip of the created directory will be produced.

2. java -jar Folderit-x.x.x.jar
If no cmd option is given, then by default files in the directory of execution are moved to new directories according to the 
file extension.

## Application usage v1.1.0

1. java -jar Folderit-x.x.x.jar -p [keyword1,keyword2...] -a [alias1,alias2,...] zip
The "-p" option indicates cmd line options are activated, keywords are the options which matches with the names of the files in 
the directory where this jar is executed. If a matching file name containing the keyword specified is found then a directory is
created having the same name. The files which matched with the keyword are moved into the newly created directory.

If you wish to give custom directory names to the matched files, then you can provide alias name with "-a" option. The matching
files with keywords will be moved to a new directory with the given alais name. The mapping is keyword1->alias1.
The "zip" is a optional parameter if provided then a zip of the created directory will be produced.

2. java -jar Folderit-x.x.x.jar
If no cmd option is given, then by default files in the directory of execution are moved to new directories according to the 
file extension.


### Direct download link

https://github.com/amardeep24/FolderIt/raw/master/Folderit-1.0.0.jar


