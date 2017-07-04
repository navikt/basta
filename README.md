# Environments

Jenkins build job at: http://aura.adeo.no/job/basta_pipeline/
Development host in tpr-u1 at: https://e34jbsl01667.devillo.no:8443
Production host: https://basta.adeo.no 

# Installation

Installed through the deploy job at Jenkins: http://aura.adeo.no/view/job/Deploy_Basta/

See Fasit for existing installations: http://fasit.adeo.no/applications/edit?18&application=272984

# Security

Access levels are controlled through AD groups. These map to access to the environment classes _u_, _t_, _q_ and _p_. 
This is implemented in the class _no.nav.aura.basta.User_. Currently this is the setup: 

<table>
<tr><th>Class</th><th>Role</th><th>Role-group-mapping in Fasit</th></tr>
<tr><td>u</td><td>ROLE_USER</td><td>None (authenticated user)</td></tr>
<tr><td>t</td><td>ROLE_OPERATIONS</td><td>http://fasit.adeo.no/resources?10&resourceAlias=env-config.operations</td></tr>
<tr><td>q</td><td>ROLE_PROD_OPERATIONS</td><td>http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations</td></tr>
<tr><td>p</td><td>ROLE_PROD_OPERATIONS</td><td>http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations</td></tr>
</table>

# Testing

In addition to normal Maven Surefire JUnit tests in java we have a integration test based on phantomjs run by Maven Failsafe. 
This test depends on phantomjs on path or a system property phantomjs.binary.path=/your/path/to/phantom . Integrationtests can be skipped with mvn install -DskipITsWeb module for basta

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

