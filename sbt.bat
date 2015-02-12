set SCRIPT_DIR=%~dp0
java -Dsbt.log.noformat=true -Dfile.encoding=UTF8 -XX:ReservedCodeCacheSize=512M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512M -Xmx2G -Xss2M -jar "%SCRIPT_DIR%\sbt-launch-0.13.5.jar" %*
