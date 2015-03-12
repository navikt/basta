# Environments

Jenkins build job at: http://aura.devillo.no/job/build_basta/
Development host in tpr-u1 at: https://e34jbsl01050.devillo.no:8443
Production host in q1 at: https://basta.adeo.no 

# Installation

Installed through the deploy job at Jenkins: http://aura.devillo.no/view/Deploy/job/Deploy_Application/

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

In addition to normal Maven Surefire JUnit tests in java we have Karma for testing Javascript and AngularJS. See 
http://confluence.adeo.no/display/AURA/Karma for installation of Karma.    