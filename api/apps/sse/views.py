import json
import random

from django.http import HttpResponse
from django.views.generic import TemplateView, View
from django.views.decorators.csrf import csrf_exempt

from django_sse.redisqueue import RedisQueueView
from django_sse.redisqueue import send_event

class Poke(View):
    def get(self, request):
        data = {'result': str(random.randint(1, 1000)), 'status': 'ok'}
        send_event('random_int', json.dumps(data))

        send_event('bogus', 'bogus test')

        return HttpResponse('OK')


class IndexPage(TemplateView):
    template_name = 'index.html'


class SSE(RedisQueueView):
    pass
