# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'PuppetFact'
        db.create_table(u'core_puppetfact', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=64)),
            ('value', self.gf('django.db.models.fields.CharField')(default='', max_length=256)),
            ('vm', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['core.Vm'])),
        ))
        db.send_create_signal(u'core', ['PuppetFact'])

        # Adding model 'Disk'
        db.create_table(u'core_disk', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('size', self.gf('django.db.models.fields.CharField')(default='s', max_length=1)),
            ('vm', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['core.Vm'])),
        ))
        db.send_create_signal(u'core', ['Disk'])

        # Adding model 'Template'
        db.create_table(u'core_template', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=64)),
            ('order', self.gf('django.db.models.fields.related.ForeignKey')(to=orm['core.Order'])),
        ))
        db.send_create_signal(u'core', ['Template'])

        # Adding model 'Vm'
        db.create_table(u'core_vm', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('guest_os', self.gf('django.db.models.fields.CharField')(default='rhel60', max_length=16)),
            ('size', self.gf('django.db.models.fields.CharField')(default='s', max_length=1)),
            ('description', self.gf('django.db.models.fields.CharField')(default='', max_length=256)),
            ('vm_type', self.gf('django.db.models.fields.CharField')(default='ap', max_length=2)),
        ))
        db.send_create_signal(u'core', ['Vm'])

        # Adding field 'Order.multisite'
        db.add_column(u'core_order', 'multisite',
                      self.gf('django.db.models.fields.BooleanField')(default=False),
                      keep_default=False)


    def backwards(self, orm):
        # Deleting model 'PuppetFact'
        db.delete_table(u'core_puppetfact')

        # Deleting model 'Disk'
        db.delete_table(u'core_disk')

        # Deleting model 'Template'
        db.delete_table(u'core_template')

        # Deleting model 'Vm'
        db.delete_table(u'core_vm')

        # Deleting field 'Order.multisite'
        db.delete_column(u'core_order', 'multisite')


    models = {
        u'core.disk': {
            'Meta': {'object_name': 'Disk'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'size': ('django.db.models.fields.CharField', [], {'default': "'s'", 'max_length': '1'}),
            'vm': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['core.Vm']"})
        },
        u'core.order': {
            'Meta': {'object_name': 'Order'},
            'application': ('django.db.models.fields.CharField', [], {'max_length': '32', 'blank': 'True'}),
            'changeDeployUser': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'createApplication': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'description': ('django.db.models.fields.TextField', [], {'blank': 'True'}),
            'environmentClass': ('django.db.models.fields.CharField', [], {'max_length': '8', 'blank': 'True'}),
            'environmentID': ('django.db.models.fields.CharField', [], {'max_length': '16', 'blank': 'True'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'middleware': ('django.db.models.fields.CharField', [], {'max_length': '4', 'blank': 'True'}),
            'multisite': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'orderType': ('django.db.models.fields.CharField', [], {'max_length': '32', 'blank': 'True'}),
            'owner': ('django.db.models.fields.CharField', [], {'max_length': '16', 'blank': 'True'}),
            'portfolio': ('django.db.models.fields.CharField', [], {'max_length': '32', 'blank': 'True'}),
            'status': ('django.db.models.fields.CharField', [], {'default': "'I'", 'max_length': '1'}),
            'updateEnvConfig': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'updated': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'}),
            'zone': ('django.db.models.fields.CharField', [], {'default': "'fss'", 'max_length': '8', 'blank': 'True'})
        },
        u'core.puppetfact': {
            'Meta': {'object_name': 'PuppetFact'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '64'}),
            'value': ('django.db.models.fields.CharField', [], {'default': "''", 'max_length': '256'}),
            'vm': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['core.Vm']"})
        },
        u'core.template': {
            'Meta': {'object_name': 'Template'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '64'}),
            'order': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['core.Order']"})
        },
        u'core.vm': {
            'Meta': {'object_name': 'Vm'},
            'description': ('django.db.models.fields.CharField', [], {'default': "''", 'max_length': '256'}),
            'guest_os': ('django.db.models.fields.CharField', [], {'default': "'rhel60'", 'max_length': '16'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'size': ('django.db.models.fields.CharField', [], {'default': "'s'", 'max_length': '1'}),
            'vm_type': ('django.db.models.fields.CharField', [], {'default': "'ap'", 'max_length': '2'})
        }
    }

    complete_apps = ['core']