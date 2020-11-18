@echo off

if defined JAVA_HOME goto updateVersion
set /p JAVA_HOME="Enter path to jdk:"

:updateVersion
%JAVA_HOME%/bin/java FabricUpdater.java

gradlew build