'use strict';

var angular = require('angular');

module.exports = ['$location', 'User', function ($location, User) {
    var vm = this;

    User.onchange(function () {
        vm.isSuperuser = User.isSuperuser();
    });

    function menuItem(header, description, icon, url, requireSuperuser) {
        return {header: header, description: description, icon: icon, url: url, requireSuperuser: requireSuperuser}
    }

    this.menu = [
        menuItem("WAS", 'Node', 'websphere.png', '/was_node_order'),
        menuItem("WAS", 'Deployment Manager', 'websphere.png', '/was_dmgr_order'),
        menuItem("BPM", 'Node', 'websphere.png', '/bpm_node_order'),
        menuItem("BPM", 'Deployment Manager', 'websphere.png', '/bpm_dmgr_order'),
        menuItem("JBoss", 'Application server', 'jboss.png', '/jboss_order'),
        menuItem("WildFly", 'Application server', 'wildfly.png', '/wildfly_order'),
        menuItem("Liberty", 'Application server', 'liberty.png', '/liberty_order'),
        menuItem("Credentials", 'for Service user', 'security.png', '/serviceuser_credential_order'),
        menuItem("Certificate", 'for Service user', 'security.png', '/serviceuser_certificate_order'),
        menuItem("OpenAM", 'Server', 'openam.png', '/openam_server_order'),
        menuItem("OpenAM", 'Proxy', 'openam.png', '/openam_proxy_order'),
        menuItem("Red Hat", 'Linux', 'redhat.png', '/linux_order'),
        menuItem("Database", 'Oracle', 'oracle.png', '/oracle_order'),
        menuItem("Windows", 'Server', 'windows.png', '/windows_order'),
        menuItem("BIG-IP", 'Load Balancer Config', 'big-ip.png', '/bigip_order'),
        menuItem("WebSphere MQ", 'Queue', 'mq.png', '/mq_queue_order'),
        menuItem("WebSphere MQ", 'Topic', 'mq.png', '/mq_topic_order'),
        menuItem("WebSphere MQ", 'Channel', 'mq.png', '/mq_channel_order'),
        menuItem("Developer Tools", "Jenkins etc. in devillo", 'devtools.png', '/devtools_order'),
        menuItem("IApp tools", "Available via VPN", 'devtools-iapp.png', '/devtools_iapp_order')
    ];

    this.goTo = function (url) {
        $location.url(url);
    };
}];