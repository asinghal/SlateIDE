
rem Please define the Java home here
set JAVA_HOME=%JAVA_HOME%

rem Please define the Scala home here
set SCALA_HOME=%SCALA_HOME%

rem assuming we are in bin directory, we need to go a level up
cd ..
set SLATE_HOME=.

set CLASSPATH=%SLATE_HOME%\conf;%SLATE_HOME%\templates;%SLATE_HOME%\lib\lucene-core-3.3.0.jar;%SLATE_HOME%\lib\gson-2.0.jar;%SLATE_HOME%\lib\scalatest-1.2-for-scala-2.8.0.RC7-SNAPSHOT.jar;%SLATE_HOME%\lib\slate-0.1.jar;%SCALA_HOME%\lib\scala-compiler.jar;"%JAVA_HOME%\lib\tools.jar"

set JAVA_OPTS=-Xms512M -Xmx512M -XX:MaxPermSize=256M

%SCALA_HOME%\bin\scala -cp %CLASSPATH% net.slate.Launch 
