node {
    def releaseVersion // metadata
    def application = "basta"
    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"
    def npm = "/usr/bin/npm"
    def node = "/usr/bin/node"
    def gulp = "${node} ./node_modules/gulp/bin/gulp.js"
    def protractor = "./node_modules/protractor/bin/protractor"
    def retire = "./node_modules/retire/bin/retire"
    def appConfig = "app-config.yaml"
    def dockerRepo = "navikt"
    def groupId = "nais"

    deleteDir()

    stage("checkout") {
	    git url: "https://github.com/navikt/${application}.git", branch: 'snyk_issues_fix'
	}


    try {
	stage("initialize") {
            releaseVersion = sh(script: 'echo $(date "+%Y-%m-%d")-$(git --no-pager log -1 --pretty=%h)', returnStdout: true).trim()

	    sh 'echo "Verifying that no snapshot dependencies is being used."'
            sh 'if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi'
            sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
        }

        stage("build application") {
            withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                sh "${mvn} clean"
                sh "${npm} install"
                sh "${gulp} dist"
            }

            sh "${mvn} install -Djava.io.tmpdir=/tmp/${application} -B -e"
        }

        stage("code analysis") {
            // Junit tests
            junit '**/surefire-reports/*.xml'

            sh "${mvn} checkstyle:checkstyle pmd:pmd findbugs:findbugs"
            findbugs computeNew: true, defaultEncoding: 'UTF-8', pattern: '**/findbugsXml.xml'
       }

        stage("test application") {
            wrap([$class: 'Xvfb']) {
                sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner " +
                "-Dstart-class=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec" +
                ".classpathScope=test &"
                sh "sleep 20"
                retry("3".toInteger()) {
                    sh "${protractor} ./src/test/js/protractor_config.js"
                }
                sh "pgrep -f StandaloneBastaJettyRunner | xargs -I% kill -9 %"
            }
        }

        stage("release version") {
            sh "sudo docker build --build-arg version=${releaseVersion} --build-arg app_name=${application} -t ${dockerRepo}/${application}:${releaseVersion} ."
        }

	      stage("publish artifact") {
	         withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
	            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'naviktdocker', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh "docker login -u ${env.USERNAME} -p ${env.PASSWORD}"
                    sh "sudo docker push ${dockerRepo}/${application}:${releaseVersion}"
                }
            }
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexusUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -s -F r=m2internal -F hasPom=false -F e=yaml -F g=${groupId} -F a=${application} -F " +
                    "v=${releaseVersion} -F p=yaml -F file=@${appConfig} -u ${env.USERNAME}:${env.PASSWORD} http://maven.adeo.no/nexus/service/local/artifact/maven/content"
            }
        }

        stage("deploy to preprod") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", \"fasitEnvironment\": \"u1\", \"zone\": \"fss\", \"namespace\": \"default\", \"fasitUsername\": \"${env.USERNAME}\", \"fasitPassword\": \"${env.PASSWORD}\"}\' https://daemon.nais.preprod.local/deploy"
            }
        }

        // Add test of preprod instance here

       stage("Ship it?") {
            timeout(time: 2, unit: 'DAYS') {
                def message = "\nreleased version: ${releaseVersion}\nbuild #: ${env.BUILD_URL}\nShip it? ${env.BUILD_URL}input\n"
                slackSend channel: '#nais-ci', message: "${env.JOB_NAME} completed successfully\n${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'
                input message: 'Deploy to prod? ', ok: 'Proceed', submitter: '0000-ga-aura'
            }
        }

        stage("deploy to prod") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", " +
                    "\"fasitEnvironment\": \"p\", \"zone\": \"fss\", \"namespace\": \"default\", \"fasitUsername\": \"${env.USERNAME}\", \"fasitPassword\": \"${env.PASSWORD}\"}\' https://daemon.nais.adeo.no/deploy"
            }
        }

        def message = ":nais: Successfully deployed ${application}:${releaseVersion} to prod\nhttps://${application}.adeo.no"
        slackSend channel: '#nais-ci', message: "${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'

        if (currentBuild.result == null) {
            currentBuild.result = "SUCCESS"
        }
    } catch (e) {
        if (currentBuild.result == null) {
            currentBuild.result = "FAILURE"
        }

        def message = ":shit: ${application} pipeline failed. See jenkins for more info ${env.BUILD_URL}\n"
        slackSend channel: '#nais-ci', message: "${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'
        throw e
    }
}
