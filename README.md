Basta
=====



## Environments

| Service        | URL                                            |
| -------------- | ---------------------------------------------- |
| Jenkins        | http://aura.adeo.no/job/basta_docker_pipeline/ |
| Pre-production | https://basta.dev.intern.nav.no                |
| Production     | https://basta.intern.nav.no                    |


## Deployment

Deployed using the above mentioned Jenkins pipeline.

## Development

The application can be started by running the `StandaloneBastaJettyRunner` class/main method. You can log in
with username "prodadmin" and password "prodadmin". The app will listen on http://localhost:1337.

When compiling and building in environments without access to Oracle Database Server the DatabaseScriptsTest test needs to be disabled.
This can be done by adding -Dtest=\!DatabaseScriptsTest.java to the maven command: `mvn -Dtest=\!DatabaseScriptsTest.java clean install`

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


## Sources

| /src/main/java      | Java stuff                      |
| /src/main/resources | Java-config stuff               |

