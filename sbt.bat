set SCRIPT_DIR=%~dp0
java -Dfile.encoding=UTF8 -XX:ReservedCodeCacheSize=1G -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1G -Xmx2G -Xss2M -jar "%SCRIPT_DIR%\sbt-launch-0.13.5.jar" %*
