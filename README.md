
# Installation #

How to install on a new server. From scratch

## Packages ##

* yum install postgresql postgresql-server
* yum install nginx
* yum install python-pip  # need this to install the package-bundle created on dev-machine (machine with inet)
* yum install libevent-devel

## User ##

* useradd -m bestillingsweb

## Project ##

### From dev machine with inet to server in NAV ###

#### Dev machine with inet ####

* { ~/bestillingsweb }$ source venv/bin/activate
* { ~/bestillingsweb }$ pip bundle bestillingsweb.pybundle -r requirements.txt
* { ~/bestillingsweb }$ cd ..
* { ~ }$ tar -zcvf bestillingsweb.tgz --exclude=bestillingsweb/venv --exclude=bestillingsweb/www/node_modules --exclude=\*.pyc bestillingsweb/

#### Server in NAV ####

* Copy over the bestillingsweb.tgz
* Do the generic installation steps, useradd, and packages
* { ~ }$ mkdir bestillingsweb && cd bestillingsweb
* { ~/bestillingsweb }$ virtualenv venv
* { ~/bestillingsweb }$ pip install --no-deps bestillingsweb.pybundle
* { ~/bestillingsweb }$ source venv/bin/activate
* { ~/bestillingsweb }$ ./manage.py collectstatic