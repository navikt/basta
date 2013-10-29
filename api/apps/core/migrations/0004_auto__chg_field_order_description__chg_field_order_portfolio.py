# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):

        # Changing field 'Order.description'
        db.alter_column(u'core_order', 'description', self.gf('django.db.models.fields.TextField')(default=''))

        # Changing field 'Order.portfolio'
        db.alter_column(u'core_order', 'portfolio', self.gf('django.db.models.fields.CharField')(default='', max_length=32))

    def backwards(self, orm):

        # Changing field 'Order.description'
        db.alter_column(u'core_order', 'description', self.gf('django.db.models.fields.TextField')(null=True))

        # Changing field 'Order.portfolio'
        db.alter_column(u'core_order', 'portfolio', self.gf('django.db.models.fields.CharField')(max_length=32, null=True))

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