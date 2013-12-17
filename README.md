# Installasjon #

Installeres via deploy-jobben p� Jenkins: http://aura.devillo.no/view/Deploy/job/Deploy_Application/

Se Fasit for eksisterende installasjoner: http://fasit.adeo.no/applications/edit?18&application=272984

# Sikkerhet #

Tilgangsniv�er er styrt gjennom grupper som gir tilgang ut i fra milj�klassene u, t, q og p. Idag er disse
satt opp slik:

| Klasse | Rolle                | Rolle-mapping i Fasit |
|--------|----------------------|-----------------------|
|   u    | ROLE_USER            | Ingen (innlogget bruker) |
|   t    | ROLE_OPERATIONS      | http://fasit.adeo.no/resources?10&resourceAlias=env-config.operations |
|   q    | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |
|   p    | ROLE_PROD_OPERATIONS | http://fasit.adeo.no/resources?12&resourceAlias=env-config.prodoperations |
