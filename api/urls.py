from django.conf.urls import patterns, include, url

from api.apps import sse
from api.apps.core import views

#urlpatterns = patterns('',
#    url(r'^_sse/$', sse.views.SSE.as_view(), name='sse'),
#    url(r'^_sse/poke/$', sse.views.Poke.as_view(), name='poke'),
#    #url(r'^$', views.IndexPage.as_view(), name='index'),
#)

urlpatterns = patterns('api.apps.core.views',
    url(r'^api/$', 'api_root'),

    url(r'^api/helper$', 'helper_root', name='helper_root'),
    url(r'^api/helper/fasit-environments$', 'get_fasit_environments', name='fasit-environments'),
    url(r'^api/helper/fasit-applications$', 'get_fasit_applications', name='fasit-applications'),

    url(r'^api/order$', views.OrderList.as_view(), name='order-list'),
    url(r'^api/order/(?P<pk>[0-9]+)$', views.OrderDetail.as_view(), name='order-detail'),

    url(r'^api/vm$', views.VmList.as_view(), name='vm-list'),
    url(r'^api/vm/(?P<pk>[0-9]+)$', views.VmDetail.as_view(), name='vm-detail'),

    url(r'^api/template$', views.TemplateDetail.as_view(), name='template-detail'),

)