from xml.dom.minidom import parseString

def xml_item_fix(s):
    if type(s) in (str, unicode):
        s = s.replace('&', '&amp;')
        s = s.replace('"', '&quot;')
        s = s.replace('\'', '&apos;')
        s = s.replace('<', '&lt;')
        s = s.replace('>', '&gt;')

    if isinstance(s, bool):
        s = 'true' if s else 'false'

    return s

def convert(obj, name=None):
    # Up for rewrite for clearety.. Its kinda ugly right now.... :(

    output = []

    if isinstance(obj, dict):
        for k,v in obj.items():
            if isinstance(v, dict):
                if v.get('name', None) and v.get('items', None):
                    output.append('<%s>%s</%s>' % (k, convert(v['items'], name=v['name']), k))
                else:
                    k_v_xml = ''
                    for d_k, d_v in v.items():
                        k_v_xml += '<%s>%s</%s>' % (d_k, xml_item_fix(d_v), d_k)
                    output.append('<%s>%s</%s>' % (k, k_v_xml, k))
            else:
                if name:
                    k = name
                if v:
                    output.append('<%s>%s</%s>' % (k, xml_item_fix(v), k))

    elif isinstance(obj, list):
        if not name:
            name = ''
        for i in obj:
            output.append('<%s>%s</%s>' % (name, convert(i), name))

    else:
        output.append(xml_item_fix(obj))

    return ''.join(output)

def dict_to_xml(obj):
    """Converts a python object into XML"""
    output = []
    output.append('<?xml version="1.0" ?>') #  encoding="UTF-8" is added by the toprettyxml function..
    output.append('<provisionRequest>%s</provisionRequest>' % (convert(obj)))
    xml = ''.join(output)
    dom = parseString(xml)
    return dom.toprettyxml(encoding='UTF-8')

if __name__ == '__main__':
    d = {}
    d['application'] = 'Lars_slett'
    d['description'] = 'vApp description'

    d['vApps'] = {'name': 'vApp', 'items': []}

    d['vApps']['items'].append({
        'site': 'so8',
        'vms': {
            'name': 'vm',
            'items': [
                {
                    'guestOs': 'rhel60',
                    'size': 's',
                    'disk': {
                        'name': 'size',
                        'items': ['s', 'm']
                    },
                },
                {
                    'guestOs': 'rhel60',
                    'size': 'm'
                }

            ]
        }
    })

    d['vApps']['items'].append({
        'site': 'u89',
        'vms': {
            'name': 'vm',
            'items': [
                {
                    'guestOs': 'rhel60',
                    'size': 's',
                    'disk': {'name': 'size', 'items': ['s', 'm']},
                },
                {
                    'guestOs': 'rhel60',
                    'size': 'm'
                }

            ]
        }
    })


    dom = parseString(dict_to_xml(d))
    print(dom.toprettyxml(encoding='UTF-8'))