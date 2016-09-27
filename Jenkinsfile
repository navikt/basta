def mvnHome = tool "maven-3.3.9"
def mvn = "${mvnHome}/bin/mvn"
def nodeHome = tool "nodejs-6.6.0"
def npm = "${nodeHome}/bin/npm"
def node = "${nodeHome}/bin/node"
def gulp = "${node} ./node_modules/gulp/bin/gulp.js"
def protractor = "./node_modules/protractor/bin/protractor"
def application = "basta"
def commiter
def releaseVersion

pipeline {
    agent label: ""

    stages {
        stage("checkout") {
            git url: "ssh://git@stash.devillo.no:7999/aura/${application}.git"
        }

        stage("initialize metadata") {
            def pom = readMavenPom file: 'pom.xml'
            releaseVersion = pom.version.tokenize("-")[0]
            sh 'git log -1 --pretty=format:"%ae (%an)" > commiter.txt'
            commiter = readFile("commiter.txt")
        }

        stage("verify dependencies") {
            sh 'echo "Verifying that no snapshot dependencies is being used."'
            sh 'grep module pom.xml | cut -d">" -f2 | cut -d"<" -f1 > snapshots.txt'
            sh 'echo "./" >> snapshots.txt'
            sh 'while read line;do if [ "$line" != "" ];then if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi;fi;done < snapshots.txt'
        }

        stage("build frontend") {
            dir("war") {
                withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                    sh "${npm} install"
                    sh "${gulp} dist"
                }
            }
        }

        stage("test backend") {
            sh "${mvn} clean install -Djava.io.tmpdir=/tmp/${application} -B -e"
        }

        stage("test frontend") {
            wrap([$class: 'Xvfb']) {
                dir("war") {
                    sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test &"
                    sh "sleep 20"
                    sh "${protractor} ./src/test/js/protractor_config.js"
                    sh "pgrep -f StandaloneBastaJettyRunner | xargs -I% kill -9 %"
                }
            }
        }

        stage("create version") {
            sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
            sh "git commit -am \"set version to ${releaseVersion} (from Jenkins pipeline)\""
            sh "git push origin master"
            sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
            sh "git push --tags"
        }

        stage("publish artifact") {
            sh "${mvn} clean deploy -DskipTests -B -e"
        }

        stage("jilease") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "/usr/bin/jilease -jiraUrl http://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
            }
        }

        stage("deploy to prod") {
            hipchatSend color: 'GRAY', message: "deploying basta $releaseVersion to p", textFormat: true, room: 'Aura - Automatisering', v2enabled: true
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "${mvn} aura:deploy -Dapps=basta:${releaseVersion} -Denv=p -Dusername=${env.USERNAME} -Dpassword=${env.PASSWORD} -Dorg.slf4j.simpleLogger.log.no.nav=debug -B -Ddebug=true -e"
            }
        }

        stage("new dev version") {
            def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
            sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            sh "git commit -am \"updated to new dev-version ${nextVersion} after release by ${commiter}\""
            sh "git push origin master"
        }
    }

    notifications {
        success {
            script {
                def message = "Successfully deployed ${application}:${releaseVersion} to prod\nhttps://${application}.adeo.no"
                mail body: "${message}", from: "aura@jenkins", subject: "${application} ${releaseVersion} deployed to prod", to: 'DGNAVIKTAURA@adeo.no'
                hipchatSend color: 'GREEN', message: "${message}", textFormat: true, room: 'Aura - Automatisering', v2enabled: true
            }
        }

        failure {
            script {
                def message = "${application} pipeline failed. See jenkins for more info ${env.BUILD_URL}\nLast commit ${lastcommit}"
                mail body: "${message}", from: "aura@jenkins", subject: "FAILURE: ${env.JOB_NAME}", to: 'DGNAVIKTAURA@adeo.no'
                hipchatSend color: 'RED', message: "@all ${env.JOB_NAME} failed\n${message}", textFormat: true, notify: true, room: 'AuraInternal', v2enabled: true
            }
        }
    }

}

