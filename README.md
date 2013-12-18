# Installation

Installed through the deploy job at Jenkins: http://aura.devillo.no/view/Deploy/job/Deploy_Application/

See Fasit for existing installations: http://fasit.adeo.no/applications/edit?18&application=272984

# Security

Access levels are controlled through AD groups. These map to access to the environment classes _u_, _t_, _q_ and _p_. 
This is implemented in the class _no.nav.aura.basta.User_. Currently this is the setup: 

<table>
<th><td>Class</td><td>Role</td><td>Role-group-mapping in Fasit</td></th>
<tr><td>u</td><td>ROLE_USER</td><td>None (authenticated user)</td></tr>
<tr><td>t</td><td>ROLE_OPERATIONS</td><td>http://fasit.adeo.no/resources?10&resourceAlias=env-config.operations</td></tr>
<tr><td>q</td><td>ROLE_PROD_OPERATIONS</td><td>http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations</td></tr>
<tr><td>p</td><td>ROLE_PROD_OPERATIONS</td><td>http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations</td></tr>
</table>
