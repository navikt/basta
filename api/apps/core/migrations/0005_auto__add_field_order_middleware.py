# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding field 'Order.middleware'
        db.add_column(u'core_order', 'middleware',
                      self.gf('django.db.models.fields.CharField')(default='', max_length=4, blank=True),
                      keep_default=False)


    def backwards(self, orm):
        # Deleting field 'Order.middleware'
        db.delete_column(u'core_order', 'middleware')


    models = {
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
            'orderType': ('django.db.models.fields.CharField', [], {'max_length': '32', 'blank': 'True'}),
            'owner': ('django.db.models.fields.CharField', [], {'max_length': '16', 'blank': 'True'}),
            'portfolio': ('django.db.models.fields.CharField', [], {'max_length': '32', 'blank': 'True'}),
            'status': ('django.db.models.fields.CharField', [], {'default': "'I'", 'max_length': '1'}),
            'updateEnvConfig': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'updated': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'}),
            'zone': ('django.db.models.fields.CharField', [], {'default': "'fss'", 'max_length': '8', 'blank': 'True'})
        }
    }

    complete_apps = ['core']