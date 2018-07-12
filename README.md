Basta
=====



## Environments

| Service        | URL                                            | Fasit resource |
| -------------- | ---------------------------------------------- | -------------- |
| Jenkins        | http://aura.adeo.no/job/basta_docker_pipeline/ | -              |
| Development    | https://basta.nais.devillo.no                  | u1             |
| Pre-production | https://basta.nais.preprod.local               | u1             |
| Production     | https://basta.adeo.no                          | p              |


## Deployment

Deployed using the above mentioned Jenkins pipeline. See Fasit for existing installations: http://fasit.adeo.no/applications/edit?18&application=272984


## Security

Access levels are controlled through AD groups. These map to access to the environment classes `u`, `t`, `q` and `p`.
This is implemented in the class `no.nav.aura.basta.User`.
    
Currently this is the setup:

| Class | Role                 | Role-group-mapping in Fasit                                               
| ----- | -------------------- | ------------------------------------------------------------------------- |
| u     | ROLE_USER            | None (authenticated user)                                                 |
| t     | ROLE_OPERATIONS      | http://fasit.adeo.no/resources?10&resourceAlias=env-config.operations     |
| q     | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |
| p     | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |


## Testing

In addition to normal Maven Surefire JUnit tests in java, we have a integration test based on phantomjs run by Maven Failsafe. This test depends on phantomjs on path or a system property `phantomjs.binary.path=/your/path/to/phantom`. Integrationtests can be skipped with `mvn install -DskipITsWeb` module for basta.


## Sources

| /src/main/java      | Java stuff                      |
| /src/main/resources | Java-config stuff               |
| /src/main/frontend  | javascript, css and other stuff |


## How to run automated GUI tests

NB: Newer versions of firefox then 45 will not work as firefox has changed it's security to only allow signed extensions. Selenium is curently not signed. Use firefox portable v45, rename `exe`-file to `firefox.exe` and set windows path to folder where `firefox.exe` is located.

Run from war folder:
> ./node_modules/protractor/bin/protractor ./src/test/js/protractor_config.js
