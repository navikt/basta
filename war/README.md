Web module for basta

# Sources

/src/main/java -- Java stuff
/src/main/resources -- Java-config stuff
/src/main/webapp -- Java web/servlet stuff
/src/main/frontend -- javascript, css and other stuff

Build with maven and gulp

# How to run automated GUI tests

 Install gulp ```bash
 npm install -g gulp
 ```

 Make sure path to firefox executable is in PATH

 run gulp webtest to run the tests visually in firefox. This requires StandaloneBastaJettyRunner to be running
 run gulp e2e-test to run the tests headless

 In webtest.js you can specifiy what specs to run. This is conventient when developing new tests and you don't want to run the entire testsuite
