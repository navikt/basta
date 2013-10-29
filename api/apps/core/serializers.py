import json

from rest_framework import serializers

from api.apps.core.models import Order, Vm

class VmSerializer(serializers.ModelSerializer):
    url = serializers.HyperlinkedIdentityField(view_name='vm-detail')

    class Meta:
        model = Vm

class JsonField(serializers.CharField):
    # DRF's json parser for some reason breaks the json format.
    def to_native(self, obj):
        return json.dumps(obj)

    def from_native(self, data):
        # We are getting the data via a channel (angularjs :)) that sends it as json, not via eg the browsable API for DRS which
        # are forced to send as string (because it doesnt have a way to edit it as json..).
        if isinstance(data, list) or isinstance(data, dict):
            return data

        try:
            return json.loads(data)
        except ValueError:
            return []

class OrderSerializer(serializers.ModelSerializer):
    url = serializers.HyperlinkedIdentityField(view_name='order-detail')
    status_human = serializers.CharField(source='get_human_status', read_only=True)
    vm_count = serializers.CharField(source='vm_count', read_only=True)

    vm_data_json = serializers.CharField(source='get_vm_data', read_only=True)  # This will reprecent the vm_data stuff as a json, not a string
    vm_data = JsonField(source='get_vm_data', read_only=False, required=False, default=[])
    xml = serializers.CharField(read_only=True)

    class Meta:
        model = Order

    def validate_vm_data(self, attrs, source):
        try:
            attrs['get_vm_data']
        except KeyError:
            return attrs

        attrs['vm_data'] = attrs.pop('get_vm_data')
        return attrs
    #    #if self.object:
    #    set_vm_data_json(attrs['get_vm_data_json'], self.object)
    #    #try:
    #    #    attrs.pop('get_vm_data_json')
    #    #except KeyError:
    #    #    pass
    #    return attrs
