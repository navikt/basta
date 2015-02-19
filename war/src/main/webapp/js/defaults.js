defaults = {
    APPLICATION_SERVER: {
        sugar:{
            imageName: 'jboss.png',
            header: 'JBoss',
            description:'Application Server'
        },
        nodeTypeName: 'JBoss Application Server',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        applicationMappingName: '',
        serverCount: 1,
        serverSize: 's',
        disk: false,
        middleWareType: 'jb'
    },
    WAS_NODES: {
        sugar:{
            imageName: 'websphere.png',
            header: 'WAS',
            description:'Node'
        },
        nodeTypeName: 'WAS Node',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        applicationMappingName: '',
        serverCount: 1,
        serverSize: 's',
        disk: false,
        ldapUserCredential: '',
        wasAdminCredential: '',
        middleWareType: 'wa'

    },
    WAS_DEPLOYMENT_MANAGER: {
        sugar:{
            imageName: 'websphere.png',
            header: 'WAS',
            description:'Deployment Manager'
        },
        nodeTypeName: 'WAS Deployment Manager',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        serverSize: 's',
        ldapUserCredential: '',
        wasAdminCredential: ''
    },
    BPM_DEPLOYMENT_MANAGER: {
        sugar:{
            imageName: 'websphere.png',
            header: 'BPM',
            description:'Deployment Manager'
        },
        nodeTypeName: 'BPM Deployment Manager',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        serverSize: 's',
        commonDatasource: '',
        cellDatasource: '',
        ldapUserCredential: '',
        wasAdminCredential: '',
        bpmServiceCredential: ''
    },
    BPM_NODES: {
        sugar:{
            imageName: 'websphere.png',
            header: 'BPM',
            description:'Node'
        },
        nodeTypeName: 'BPM Node',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        serverCount: 1,
        commonDatasource: '',
        ldapUserCredential: '',
        wasAdminCredential: '',
        bpmServiceCredential: ''
    },
    PLAIN_LINUX: {
        sugar:{
            imageName: 'redhat.png',
            header: 'Red Hat',
            description:'Linux'
        },
        nodeTypeName: 'Plain Linux Server',
        environmentClass: 'u',
        zone: 'fss',
        serverSize: 's'
    }
};
