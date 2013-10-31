import re
import json
import random
import urllib2

import xml.etree.ElementTree as et

from django.http import HttpResponse
from django.views.generic import View
from django.shortcuts import get_object_or_404

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework.reverse import reverse
from rest_framework.exceptions import APIException
from rest_framework import generics, viewsets

from django_sse.redisqueue import RedisQueueView
from django_sse.redisqueue import send_event

from api.apps.core.models import Order, Vm
from api.apps.core.serializers import OrderSerializer, VmSerializer

class Poke(View):
    def get(self, request):
        data = {'result': str(random.randint(1, 1000)), 'status': 'ok'}
        send_event('random_int', json.dumps(data))

        send_event('bogus', 'bogus test')

        return HttpResponse('OK')

class SSE(RedisQueueView):
    pass


class MultipleObjectsReturned(APIException):
    status_code = 418  # I'm a teapot.. There isnt any "good" status codes for multiple objects, need one, errors.. :)
    detail = 'Multiple objects found..'

@api_view(('GET',))
def api_root(request, format=None):
    return Response({
        'order': reverse('order-list', request=request, format=format),
        'vm': reverse('vm-list', request=request, format=format),
        'template': reverse('template-detail', request=request, format=format),
        'helper': reverse('helper_root', request=request, format=format),
    })

@api_view(('GET',))
def helper_root(request, format=None):
    return Response({
        'fasit-environments': reverse('fasit-environments', request=request, format=format),
        'fasit-applications': reverse('fasit-applications', request=request, format=format),
    })


@api_view(('GET',))
def get_fasit_environments(request, format=None):
    """
    This is a read-only list which we are grabbing from https://fasit.adeo.no/conf/environments. We parse it and makes it angular-friendly before displaying :)
    """
    environments = []
    environments_res = urllib2.urlopen('https://fasit.adeo.no/conf/environments')
    environments_xml = et.fromstring(environments_res.read())

    env_class = request.QUERY_PARAMS.get('env_class', None)

    for e in environments_xml:
        entry_env_class = e.find('envClass').text
        if env_class:
            if not env_class[0] == entry_env_class:
                continue

        environments.append({
            'envClass': entry_env_class,
            'name': e.find('name').text
            })

    # Natural sorting our dict by name..
    convert = lambda text: int(text) if text.isdigit() else text.lower()
    environments = sorted(environments, key=lambda e: [ convert(c) for c in re.split('([0-9]+)', e['name'])])

    return Response(environments)

@api_view(('GET',))
def get_fasit_applications(request, format=None):
    """
    This is a read-only list which we are grabbing from https://fasit.adeo.no/conf/applications. We parse it and makes it angular-friendly before displaying :)
    """
    applications = []
    applications_res = urllib2.urlopen('https://fasit.adeo.no/conf/applications')
    applications_xml = et.fromstring(applications_res.read())

    for a in applications_xml:
        applications.append({
            'name': a.find('name').text
            })

    applications = sorted(applications, key=lambda e: e['name'].lower())
    return Response(applications)



class VmList(generics.ListCreateAPIView):
    queryset = Vm.objects.all()
    serializer_class = VmSerializer

class VmDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Vm.objects.all()
    serializer_class = VmSerializer

class OrderList(generics.ListCreateAPIView):
    serializer_class = OrderSerializer

    def get_queryset(self):
        queryset = Order.objects.all()
        return queryset

class OrderDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Order.objects.all()
    serializer_class = OrderSerializer
    #permission_classes = (permissions.IsAuthenticatedOrReadOnly,)

class TemplateDetail(generics.RetrieveAPIView):
    """
    # Info
    Used to get the template we are looking for based on a couple of GET parameters.
    All params are case insensitive. So it doesnt mather what case you use..
    You will need ALL parameters to find anything...

    # Params
    * `type`: Which is one of ["applicationPlatform", "infrastructureService", "offtheshelf"]
    * `application`: Which is example "was", "confluence", "whatever"..

    # Example:
    Via commandline; `curl http://ORDERPORTALURL/api/template?type=offtheshelf&application=confluence`
    """

    queryset = Order.objects.filter(status='T')
    serializer_class = OrderSerializer

    def get_object(self):
        request_params = {
            'orderType__iexact': self.request.GET.get('type', ''),
            'application__iexact': self.request.GET.get('application', '')
        }

        try:
            return get_object_or_404(self.queryset, **request_params)
        except self.queryset.model.MultipleObjectsReturned:
            # This shoulnt happend.. You cant add orders like this to the db, clean() in the models denies it.
            # However, it is here for testing stuff when developing. :)
            raise MultipleObjectsReturned