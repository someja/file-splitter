# file-splitter  

[English ReadMe](https://github.com/someja/file-splitter/blob/master/README.en.md)

#### 介绍
文件分割器, 可将大文件分割为多个小文件, 支持自定义分割大小等参数.


#### 安装教程
* 直接下载
[file-splitter](https://github.com/someja/file-splitter/releases/download/V0.0.1/file-splitter-jar-with-dependencies.jar)


* 手动安装如下:  
1. 首先确保安装JDK8, MAVEN  
2. 克隆本代码, 然后在项目根目录命令行下执行`mvn package`打包, 在target下生成`file-splitter-jar-with-dependencies.jar`即可.  



#### 使用说明

1. 假设要将`D:\tmp\bigfile.txt`文件分割到`D:\tmp\output`文件夹下, 需执行如下命令:  
`java -jar target/file-splitter-jar-with-dependencies.jar -s D:\tmp\bigfile.txt -d D:\tmp\output`  
-s 即源文件  
-d 即目的文件夹, 切割后的文件会生成在这个下面   
以上2项为必填项.  

2. 为了方便使用, 本工具还支持以下参数(可选):  
`-allowEmptyLine`             默认会忽略空行,此选项会将空行写入到目的文件  
`-fileIndex <arg>`            文件命名起始索引（数字）,默认从1开始命名  
`-notClearDirAtFirst`         执行前不要清空目的文件夹,默认会清空目的文件夹  
`-splitSize <arg>`            切割大小,默认字节,支持k,m,g,默认50M  
`-suffix <arg>`               切割后的文件后缀,默认是取的源文件的后缀  

3. 可通过API方式调用.    
  
Maven坐标:  
```  
<dependency>
	<groupId>com.github.someja</groupId>
	<artifactId>file-splitter</artifactId>
	<version>0.0.1</version>
</dependency>	
```  


#### 联系方式  
有问题可联系: 2358236929@qq.com
