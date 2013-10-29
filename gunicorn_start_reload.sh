#!/bin/bash

set -e

my_dir="$(cd $(dirname ${BASH_SOURCE:-$0});pwd)"
cd ${my_dir}

PIDFILE="${my_dir}/main.pid"
LOGFILE="${my_dir}/log/main.log"
LOGDIR=$(dirname ${LOGFILE})

source venv/bin/activate

test -d ${LOGDIR} || mkdir -p ${LOGDIR}
test -f ${PIDFILE} || touch $PIDFILE

kill -HUP $(cat $PIDFILE) || exec gunicorn_django -c gunicorn.conf --log-file=${LOGFILE} -p ${PIDFILE} -D # --log-level=debug
