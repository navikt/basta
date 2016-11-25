Web module for basta

# Sources

/src/main/java -- Java stuff
/src/main/resources -- Java-config stuff
/src/main/webapp -- Java web/servlet stuff
/src/main/frontend -- javascript, css and other stuff



# How to run automated GUI tests

NB Newer versions of firefox then 45 will not work as firefox has changed it's security to only allow signed extensions. Selenium is curently not signed.
Use firefox portable v45, rename exe file to firefox.exe and set windows path to folder where firefox.exe is located.

Run from war folder
 ./node_modules/protractor/bin/protractor ./src/test/js/protractor_config.js

