# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding field 'Order.count'
        db.add_column(u'core_order', 'count',
                      self.gf('django.db.models.fields.IntegerField')(default=1),
                      keep_default=False)


    def backwards(self, orm):
        # Deleting field 'Order.count'
        db.delete_column(u'core_order', 'count')


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
            'count': ('django.db.models.fields.IntegerField', [], {'default': '1'}),
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