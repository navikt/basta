defaults = {
    APPLICATION_SERVER: {
        nodeTypeName: 'JBoss Application Server',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        applicationName: '',
        serverCount: 1,
        serverSize: 's',
        disk: false,
        middleWareType: 'jb'
    },
    WAS_NODES: {
        nodeTypeName: 'WAS Node',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        applicationName: '',
        serverCount: 1,
        serverSize: 's',
        disk: false,
        wasAdminCredential: null,
        middleWareType: 'wa'

    },
    WAS_DEPLOYMENT_MANAGER: {
        nodeTypeName: 'WAS Deployment Manager',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        wasAdminCredential: null
    },
    BPM_DEPLOYMENT_MANAGER: {
        nodeTypeName: 'BPM Deployment Manager',
        environmentClass: 'u',
        zone: 'fss',
        environmentName: '',
        commonDatasource: null,
        cellDatasource: null,
        wasAdminCredential: null,
        bpmServiceCredential: null
    },
    BPM_NODES: {
        nodeTypeName: 'BPM Node',
            environmentClass: 'u',
            zone: 'fss',
            environmentName: '',
            commonDatasource: null,
            wasAdminCredential: null,
            bpmServiceCredential: null
    },
    PLAIN_LINUX: {
        nodeTypeName: 'Plain Linux Server',
            environmentClass: 'u',
            zone: 'fss',
            serverSize: 's'
    }
};
