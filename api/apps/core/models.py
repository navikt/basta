import uuid
import os
from subprocess import Popen, PIPE

from django.db import models
from django.core.exceptions import ValidationError

from api.apps.core.lib import xml_helper

def initialize_order(sender, instance, **kwargs):
    if instance.orch_response != '':
        return None # We have sent the order already

    if instance.status == 'Q':
        file_name = '/tmp/orcestrator_order_%s.xml' % str(uuid.uuid4())

        with open(file_name, 'w') as fp:
            fp.write(instance.xml)

        # Call java jar file and store results..
        p = Popen(['ls', '-l'], stdout=PIPE)
        p.wait()
        instance.orch_response = p.stdout.read()
        instance.status = 'A' # Active
        instance.save()
        os.unlink(file_name)

def generate_xml(sender, instance, **kwargs):
    if instance.orch_response != '':
        return None # We have sent the order already

    if instance.environmentClass == 'qa' and instance.multisite:
        environmentClass = 'preprod'
    else:
        environmentClass = instance.environmentClass

    x = {}
    x['application'] = instance.application
    x['description'] = instance.description
    x['environmentClass'] = environmentClass
    x['environmentId'] = instance.environmentID
    x['zone'] = instance.zone
    x['expires'] = instance.expire.strftime('%Y-%m-%d') if instance.expire else ''
    x['orderedBy'] = instance.owner # FIXME, should be AD user!
    x['owner'] = instance.owner
    x['portfolio'] = instance.portfolio
    x['projectId'] = instance.project_id
    x['updateEnvConfig'] = instance.updateEnvConfig
    x['changeDeployUser'] = instance.changeDeployUser
    x['envConfTestEnv'] = instance.envConfTestEnv
    x['createApplication'] = instance.createApplication
    x['engineeringBuild'] = instance.engineeringBuild
    x['role'] = instance.role

    x['vApps'] = {'name': 'vApp', 'items': []}

    vms = []
    for v in instance.vm_set.all():
        vm_type = instance.vm_type if instance.vm_type else 'ap'

        vm_dict = {
            'guestOs': v.guest_os,
            'size': v.size,
            'description': v.description,
            'dmz': v.dmz,
            'type': vm_type
            }

        disks = v.disk_set.all()
        if disks:
            disks_list = []
            for disk in disks:
                disks_list.append(disk.size)
            vm_dict['disk'] = {'name': 'size', 'items': disks_list}

        facts = v.puppetfact_set.all()
        if facts:
            facts_dict = {}
            for fact in facts:
                facts_dict[fact.name] = fact.value
            vm_dict['customfacts'] = facts_dict

        vms.append(vm_dict)

    x['vApps']['items'].append({
        'site': 'so8',
        'vms': {
            'name': 'vm',
            'items': vms
        }
    })

    if instance.multisite:
        x['vApps']['items'].append(x['vApps']['items'][0].copy())
        x['vApps']['items'][1]['site'] = 'u89'

    instance.xml = xml_helper.dict_to_xml(x)

    models.signals.post_save.disconnect(generate_xml, sender=Order)
    instance.save()
    models.signals.post_save.connect(generate_xml, sender=Order)

def generate_other_objects_from_json_data(sender, instance, **kwargs):
    if instance.orch_response != '':
        return None # We have sent the order already

    if instance.vm_data or instance.vm_data == [] or instance.vm_data == '':
        instance.set_vm_data(instance.vm_data)
    else:
        instance.set_vm_data(instance.get_vm_data)

class Order(models.Model):
    orderType = models.CharField(max_length=32, blank=True)
    environmentClass = models.CharField(max_length=8, blank=True)
    environmentID = models.CharField(max_length=16, blank=True, null=False)
    application = models.CharField(max_length=32, blank=True)
    vm_type = models.CharField(max_length=4, blank=True)
    zone = models.CharField(default='fss', max_length=8, blank=True)
    owner = models.CharField(max_length=16, blank=True)
    portfolio = models.CharField(max_length=32, blank=True)
    project_id = models.CharField(max_length=32, blank=True)
    role = models.CharField(max_length=8, blank=True)
    multisite = models.BooleanField(default=False)
    expire = models.DateTimeField(null=True, blank=True)

    # Storage for how the "per server" data looks like.
    # We need to store it at the object temporary when creating new orders so we are able to fetch
    # the data in the post processing job that generate the real objects based on this vm_data.
    # We wont delete it either, because then, if you want to change how the vm's looks from the api browser,
    # you would haveto generate the whole json text manually..
    vm_data = models.TextField(default='')

    xml = models.TextField(default='')

    # If this is sat, we assume that the order is sent.
    orch_response = models.TextField(default='')

    description = models.TextField(blank=True)

    updateEnvConfig = models.BooleanField(default=False)
    changeDeployUser = models.BooleanField(default=False)
    envConfTestEnv = models.BooleanField(default=True)
    createApplication = models.BooleanField(default=False)
    engineeringBuild = models.BooleanField(default=True)
    advancedEnabled = models.BooleanField(default=False)

    status = models.CharField(max_length=1, default='I')
    updated = models.DateTimeField(auto_now=True)
    created = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return str(self.pk)

    def clean(self):
        if self.status != 'T':
            return True

        request_params = {
            'orderType__iexact': self.orderType,
            'application__iexact': self.application,
            'status': 'T'
        }

        try:
            Order.objects.get(**request_params)
        except Order.DoesNotExist:
            return True
        except Order.MultipleObjectsReturned:
            raise ValidationError('type and application must be unique togheter if this is an template!')

        return True

    def vm_count(self):
        try:
            return self.vm_set.count()
        except Vm.DoesNotExist:
            return 0

    def get_vm_data(self):
        if not self:
            # We cant do this on a new (not created object)
            return None

        data = []

        for vm in self.vm_set.all():
            vm_dict = {
                'disk': [],
                'puppetFact': [],
                'guestos': vm.guest_os,
                'size': vm.size,
                'description': vm.description,
                'dmz': vm.dmz
            }
            for disk in vm.disk_set.all():
                vm_dict['disk'].append({'size': disk.size})

            for fact in vm.puppetfact_set.all():
                vm_dict['puppetFact'].append({'name':fact.name, 'value':fact.value})

            data.append(vm_dict)

        return data

    def set_vm_data(self, dict_obj):
        # Clear and set json data, since we dont really know if what we have.
        # If the user wants to add one extra disk to one vm, or remove it.. We would
        # have implemented a lot of logic to handle it. And uniq ID's all over.
        # Think this is a better solution. Remove all, add all..

        self.vm_set.all().delete()
        for vm in dict_obj:
            new_vm = Vm.objects.create(
                order=self,
                guest_os=vm['guestos'],
                size=vm['size'],
                description=vm['description'],
                dmz=vm['dmz']
                )

            for disk in vm['disk']:
                Disk.objects.create(
                    vm=new_vm,
                    size=disk['size']
                    )

            for fact in vm['puppetFact']:
                PuppetFact.objects.create(
                    vm=new_vm,
                    name=fact['name'],
                    value=fact['value']
                    )

    def get_human_status(self):
        statuses = {
            'A': 'Active', # Order is active, ie, vmware is working on it
            'E': 'Error', # Order need attention
            'C': 'Complete', # Order is complete
            'I': 'Inactive', # Order is saved, but still inactive
            'Q': 'Queue', # Order is queued
            'T': 'Template' # This order is a template
        }
        return statuses.get(self.status, '* Unknown *')
models.signals.post_save.connect(generate_other_objects_from_json_data, sender=Order)
models.signals.post_save.connect(generate_xml, sender=Order)
models.signals.post_save.connect(initialize_order, sender=Order)

class Vm(models.Model):
    order = models.ForeignKey(Order)
    guest_os = models.CharField(max_length=16, default='rhel60')
    size = models.CharField(max_length=1, default='s')
    description = models.CharField(max_length=256, default='')
    dmz = models.BooleanField(default=False)

class Disk(models.Model):
    size = models.CharField(max_length=1, default='s')
    vm = models.ForeignKey(Vm)

class PuppetFact(models.Model):
    name = models.CharField(max_length=64)
    value = models.CharField(max_length=256, default='')
    vm = models.ForeignKey(Vm)
