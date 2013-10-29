# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Deleting field 'Order.order_type'
        db.delete_column(u'core_order', 'order_type')

        # Adding field 'Order.orderType'
        db.add_column(u'core_order', 'orderType',
                      self.gf('django.db.models.fields.CharField')(default='', max_length=32),
                      keep_default=False)


    def backwards(self, orm):
        # Adding field 'Order.order_type'
        db.add_column(u'core_order', 'order_type',
                      self.gf('django.db.models.fields.CharField')(default='', max_length=32),
                      keep_default=False)

        # Deleting field 'Order.orderType'
        db.delete_column(u'core_order', 'orderType')


    models = {
        u'core.order': {
            'Meta': {'object_name': 'Order'},
            'application': ('django.db.models.fields.CharField', [], {'max_length': '32'}),
            'changeDeployUser': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'createApplication': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'created': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            'description': ('django.db.models.fields.TextField', [], {'null': 'True', 'blank': 'True'}),
            'environmentClass': ('django.db.models.fields.CharField', [], {'max_length': '8'}),
            'environmentID': ('django.db.models.fields.CharField', [], {'max_length': '16'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'orderType': ('django.db.models.fields.CharField', [], {'max_length': '32'}),
            'owner': ('django.db.models.fields.CharField', [], {'max_length': '16'}),
            'portfolio': ('django.db.models.fields.CharField', [], {'max_length': '32', 'null': 'True', 'blank': 'True'}),
            'updateEnvConfig': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'updated': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'}),
            'zone': ('django.db.models.fields.CharField', [], {'default': "'fss'", 'max_length': '8'})
        }
    }

    complete_apps = ['core']