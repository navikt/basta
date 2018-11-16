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


    try {
	stage("initialize") {
            def pom = readMavenPom file: 'pom.xml'
            releaseVersion = pom.version.tokenize("-")[0]
            changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)
            sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
        }

        stage("build application") {
            withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                sh "${mvn} clean"
                sh "${npm} install"
                sh "${gulp} dist"
            }

            sh "${mvn} install -DskipTests=true -Djava.io.tmpdir=/tmp/${application} -B -e"
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

        stage("deploy to preprod") {
            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", \"fasitEnvironment\": \"u1\", \"zone\": \"fss\", \"namespace\": \"default\", \"fasitUsername\": \"${env.USERNAME}\", \"fasitPassword\": \"${env.PASSWORD}\"}\' https://daemon.nais.preprod.local/deploy"
            }
        }


        stage("new dev version") {
            def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
            sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
            sh "git commit -m \"Releasing ${nextVersion} after release by ${committer}\" pom.xml"
             withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'navikt-ci', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                withEnv(['HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                    sh(script: "git push https://${USERNAME}:${PASSWORD}@github.com/navikt/${application}.git jwt_token")
                }
            }
        }








        if (currentBuild.result == null) {
            currentBuild.result = "SUCCESS"
        }
    } catch (e) {
        if (currentBuild.result == null) {
            currentBuild.result = "FAILURE"
        }


        throw e
    }
}
