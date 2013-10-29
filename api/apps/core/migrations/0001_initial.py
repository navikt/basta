# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Order'
        db.create_table(u'core_order', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('order_type', self.gf('django.db.models.fields.CharField')(max_length=32)),
            ('environmentClass', self.gf('django.db.models.fields.CharField')(max_length=8)),
            ('environmentID', self.gf('django.db.models.fields.CharField')(max_length=16)),
            ('application', self.gf('django.db.models.fields.CharField')(max_length=32)),
            ('zone', self.gf('django.db.models.fields.CharField')(default='fss', max_length=8)),
            ('owner', self.gf('django.db.models.fields.CharField')(max_length=16)),
            ('portfolio', self.gf('django.db.models.fields.CharField')(max_length=32, null=True, blank=True)),
            ('description', self.gf('django.db.models.fields.TextField')(null=True, blank=True)),
            ('updateEnvConfig', self.gf('django.db.models.fields.BooleanField')(default=False)),
            ('changeDeployUser', self.gf('django.db.models.fields.BooleanField')(default=False)),
            ('createApplication', self.gf('django.db.models.fields.BooleanField')(default=False)),
            ('updated', self.gf('django.db.models.fields.DateTimeField')(auto_now=True, blank=True)),
            ('created', self.gf('django.db.models.fields.DateTimeField')(auto_now_add=True, blank=True)),
        ))
        db.send_create_signal(u'core', ['Order'])


    def backwards(self, orm):
        # Deleting model 'Order'
        db.delete_table(u'core_order')


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
            'order_type': ('django.db.models.fields.CharField', [], {'max_length': '32'}),
            'owner': ('django.db.models.fields.CharField', [], {'max_length': '16'}),
            'portfolio': ('django.db.models.fields.CharField', [], {'max_length': '32', 'null': 'True', 'blank': 'True'}),
            'updateEnvConfig': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'updated': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'}),
            'zone': ('django.db.models.fields.CharField', [], {'default': "'fss'", 'max_length': '8'})
        }
    }

    complete_apps = ['core']