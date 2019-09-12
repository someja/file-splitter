# file-splitter

#### Description
split one file to smaller files, Support for custom parameters

#### Install
1. make sure have installed JDK8, Maven  
2. clone this project, and then run the command:
```
	mvn clean package
```  

#### Usage
`java -jar target/file-splitter-jar-with-dependencies.jar -s D:\tmp\bigfile.txt -d D:\tmp\output`  
this command will split the `D:\tmp\bigfile` to several files in `D:\tmp\output`  
-s source file  
-d destination dir  

other config:  
`-allowEmptyLine`             default ignore empty line. Enable this config will write the empty line into destination from source file  
`-fileIndex <arg>`            destination file started index, default 1  
`-notClearDirAtFirst`         enable this config will clear destination dir at first  
`-splitSize <arg>`            split size in bytes, support unit k,m,g. default: 50M   
`-suffix <arg>`               destination file suffix, default same as source  

#### Contact  
email: 2358236929@qq.com

