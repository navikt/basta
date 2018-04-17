node {
    def committer, changelog, releaseVersion // metadata
    def application = "basta"
    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"
    def npm = "/usr/bin/npm"
    def node = "/usr/bin/node"
    def gulp = "${node} ./node_modules/gulp/bin/gulp.js"
    def protractor = "./node_modules/protractor/bin/protractor"
    def retire = "./node_modules/retire/bin/retire"
    def appConfig = "app-config.yaml"
    def dockerRepo = "docker.adeo.no:5000"
    def groupId = "nais"

    stage("checkout") {
	git credentialsId: 'navikt-ci',
	    url: "https://github.com/navikt/${application}.git"
    }

    lastCommitMessage = sh(script: "git --no-pager log -1 --pretty=%B", returnStdout: true).trim()
    if (lastCommitMessage != null &&
        lastCommitMessage.toString().contains('Releasing ')) {
	return
    }

    try {
	stage("initialize") {
            def pom = readMavenPom file: 'pom.xml'
            releaseVersion = pom.version.tokenize("-")[0]
            changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)

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
            sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'navikt-ci', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                    sh(script: "git push https://${USERNAME}:${PASSWORD}@github.com/navikt/${application}.git --tags")
                }
            }
        }

	stage("publish artifact") {
            sh "sudo docker push ${dockerRepo}/${application}:${releaseVersion}"
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexusUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -s -F r=m2internal -F hasPom=false -F e=yaml -F g=${groupId} -F a=${application} -F " +
                    "v=${releaseVersion} -F p=yaml -F file=@${appConfig} -u ${env.USERNAME}:${env.PASSWORD} http://maven.adeo.no/nexus/service/local/artifact/maven/content"
            }
        }

        stage("deploy to dev/test") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", " +
                   "\"environment\": \"u1\", \"zone\": \"fss\", \"namespace\": \"default\", \"username\": \"${env.USERNAME}\", \"password\": \"${env.PASSWORD}\"}\' https://daemon.nais.devillo.no/deploy"
            }
        }

        stage("deploy to preprod") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", \"environment\": \"u1\", \"zone\": \"fss\", \"namespace\": \"default\", \"username\": \"${env.USERNAME}\", \"password\": \"${env.PASSWORD}\"}\' https://daemon.nais.preprod.local/deploy"
            }
        }

        // Add test of preprod instance here
        stage("new dev version") {
            def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
            sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            sh "git commit -m \"Releasing ${nextVersion} after release by ${committer}\" pom.xml"
             withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'navikt-ci', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                    sh(script: "git push https://${USERNAME}:${PASSWORD}@github.com/navikt/${application}.git master")
                }
            }
        }

        stage("jilease") {
             withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "/usr/bin/jilease -jiraUrl https://jira.adeo.no -project AURA -application ${application} -version" +
                    " $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
            }
        }

        stage("Ship it?") {
            timeout(time: 2, unit: 'DAYS') {
                def message = "\nreleased version: ${releaseVersion}\nbuild #: ${env.BUILD_URL}\nLast commit ${changelog}\nShip it? ${env.BUILD_URL}input\n"
                slackSend channel: '#nais-ci', message: "${env.JOB_NAME} completed successfully\n${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'
                input message: 'Deploy to prod? ', ok: 'Proceed', submitter: '0000-ga-aura'
            }
        }

        stage("deploy to prod") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", " +
                    "\"environment\": \"p\", \"zone\": \"fss\", \"namespace\": \"default\", \"username\": \"${env.USERNAME}\", \"password\": \"${env.PASSWORD}\"}\' https://daemon.nais.adeo.no/deploy"
            }
        }

        def message = ":nais: Successfully deployed ${application}:${releaseVersion} to prod\n${changelog}\nhttps://${application}.adeo.no"
        slackSend channel: '#nais-ci', message: "${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'

        if (currentBuild.result == null) {
            currentBuild.result = "SUCCESS"
        }
    } catch (e) {
        if (currentBuild.result == null) {
            currentBuild.result = "FAILURE"
        }

        def message = ":shit: ${application} pipeline failed. See jenkins for more info ${env.BUILD_URL}\n${changelog}"
        slackSend channel: '#nais-ci', message: "${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'
        throw e
    }
}
