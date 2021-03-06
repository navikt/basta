#!/usr/bin/env sh

echo "starting init-script for reading vault secrets"
ls -la /var/run/secrets/nais.io

if test -d /var/run/secrets/nais.io/bastaDB-creds;
then
    echo "Setting DB creds"
    export  BASTADB_USERNAME=$(cat /var/run/secrets/nais.io/bastaDB-creds/username)
    export  BASTADB_PASSWORD=$(cat /var/run/secrets/nais.io/bastaDB-creds/password)
fi

if test -d /var/run/secrets/nais.io/bastaDB-config;
then
  echo "Setting DB config"
  export BASTADB_URL="$(cat /var/run/secrets/nais.io/bastaDB-config/jdbc_url)"
  export BASTADB_ONSHOSTS="$(cat /var/run/secrets/nais.io/bastaDB-config/ons_host)"
fi

if test -d /var/run/secrets/nais.io/srvbasta;
then
  echo "Setting srvbasta creds"
  export SRVBASTA_USERNAME=$(cat /var/run/secrets/nais.io/srvbasta/username)
  export SRVBASTA_PASSWORD=$(cat /var/run/secrets/nais.io/srvbasta/password)
fi

if test -d /var/run/secrets/nais.io/user_orchestrator;
then
  echo "Setting Orchestrator creds"
  export USER_ORCHESTRATOR_USERNAME=$(cat /var/run/secrets/nais.io/user_orchestrator/username)
  export USER_ORCHESTRATOR_PASSWORD=$(cat /var/run/secrets/nais.io/user_orchestrator/password)
fi

if test -d /var/run/secrets/nais.io/basta_mq_dev;
then
  echo "Setting mq dev creds"
  export BASTA_MQ_U_USERNAME=$(cat /var/run/secrets/nais.io/basta_mq_dev/username)
  export BASTA_MQ_U_PASSWORD=$(cat /var/run/secrets/nais.io/basta_mq_dev/password)
  export BASTA_MQ_T_USERNAME=$(cat /var/run/secrets/nais.io/basta_mq_dev/username)
  export BASTA_MQ_T_PASSWORD=$(cat /var/run/secrets/nais.io/basta_mq_dev/password)
  export BASTA_MQ_Q_USERNAME=$(cat /var/run/secrets/nais.io/basta_mq_dev/username)
  export BASTA_MQ_Q_PASSWORD=$(cat /var/run/secrets/nais.io/basta_mq_dev/password)
fi

if test -d /var/run/secrets/nais.io/basta_mq_prod;
then
  echo "Setting mq prod creds"
  export BASTA_MQ_P_USERNAME=$(cat /var/run/secrets/nais.io/basta_mq_prod/username)
  export BASTA_MQ_P_PASSWORD=$(cat /var/run/secrets/nais.io/basta_mq_prod/password)
fi

if test -d /var/run/secrets/nais.io/basta_security_ca_prod;
then
  echo "Setting security ca prod creds"
  export SECURITY_CA_ADEO_USERNAME=$(cat /var/run/secrets/nais.io/basta_security_ca_prod/username)
  export SECURITY_CA_ADEO_PASSWORD=$(cat /var/run/secrets/nais.io/basta_security_ca_prod/password)
fi

if test -d /var/run/secrets/nais.io/basta_security_ca_preprod;
then
  echo "Setting security ca preprod creds"
  export SECURITY_CA_PREPROD_USERNAME=$(cat /var/run/secrets/nais.io/basta_security_ca_preprod/username)
  export SECURITY_CA_PREPROD_PASSWORD=$(cat /var/run/secrets/nais.io/basta_security_ca_preprod/password)
fi

if test -d /var/run/secrets/nais.io/basta_security_ca_test;
then
  echo "Setting security ca test creds"
  export SECURITY_CA_TEST_USERNAME=$(cat /var/run/secrets/nais.io/basta_security_ca_test/username)
  export SECURITY_CA_TEST_PASSWORD=$(cat /var/run/secrets/nais.io/basta_security_ca_test/password)
fi

if test -d  /var/run/secrets/nais.io/basta_oem;
then
  echo "Setting OEM api creds"
  export OEM_USERNAME=$(cat  /var/run/secrets/nais.io/basta_oem/username)
  export OEM_PASSWORD=$(cat  /var/run/secrets/nais.io/basta_oem/password)
fi