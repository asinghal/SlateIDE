
rem Please define the Java home here
JAVA_HOME=%JAVA_HOME%

rem Please define the Scala home here
SCALA_HOME=%SCALA_HOME%

SLATE_HOME=..

CLASSPATH=%SLATE_HOME%\conf;%SLATE_HOME%\templates;%SLATE_HOME%\lib\lucene-core-3.3.0.jar;%SLATE_HOME%\lib\scalatest-1.2-for-scala-2.8.0.RC7-SNAPSHOT.jar;%SLATE_HOME%\lib\slate-0.1.jar;%JAVA_HOME%\lib\tools.jar;%SCALA_HOME%\lib\scala-compiler.jar


%SCALA_HOME%\bin\scala -cp $CLASSPATH -Xmx512M net.slate.Launch 
