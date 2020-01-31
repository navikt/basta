
node {
    def releaseVersion
    def dockerimage
    def application = "basta"
    def mvnHome = tool "maven-3.3.9"
    def mvn = "${mvnHome}/bin/mvn"
    def npm = "/usr/bin/npm"
    def node = "/usr/bin/node"
    def gulp = "${node} ./node_modules/gulp/bin/gulp.js"
    def protractor = "./node_modules/protractor/bin/protractor"
    def retire = "./node_modules/retire/bin/retire"
    def dockerRepo = "navikt"

   try {

   deleteDir()

    def workspace = pwd()

    stage("checkout") {
        withCredentials([string(credentialsId: 'aura_infra_checkout_key', variable: 'TOKEN')]) {
            withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'HTTPS_PROXY=http://webproxy-utvikler.nav.no:8088',  'NO_PROXY=adeo.no']) {
                git url: "https://github.com/navikt/basta.git"
                dir('config') {
                    git url: "https://${TOKEN}@github.com/navikt/aura-infra.git"
                }
            }
        }
    }

    stage('info') {
        sh "ls -la"
        sh "ls -la config/basta/"
    }

	stage("initialize") {
        releaseVersion = sh(script: 'echo $(date "+%Y-%m-%d")-$(git --no-pager log -1 --pretty=%h)', returnStdout: true).trim()
        dockerimage = "${dockerRepo}/${application}:${releaseVersion}"

	    sh 'echo "Verifying that no snapshot dependencies is being used."'
            sh 'if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi'
            //sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
        }

        stage("build application") {
            withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
                //sh "${mvn} clean"
                //sh "${npm} install -DskipTests=true"
                //sh "${gulp} dist"
            }

            sh "${mvn} install -Djava.io.tmpdir=/tmp/${application} -B -e"
        }

        stage("release version") {
            sh "sudo docker build -t ${dockerimage} ."
        }

	      stage("publish artifact") {
	         withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
	            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'naviktdocker', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
                    sh "docker login -u ${env.USERNAME} -p ${env.PASSWORD}"
                    sh "sudo docker push ${dockerimage}"
                }
            }
        }

        stage('Deploy dev') {
            withCredentials([string(credentialsId: 'NAIS_DEPLOY_APIKEY', variable: 'NAIS_DEPLOY_APIKEY')]) {
                sh "echo 'Deploying ${application}:${releaseVersion} to dev-fss'"
                sh "chown -R jenkins:jenkins ${workspace}"
                sh "sudo docker run --rm -v ${workspace}/config/basta:/nais navikt/deployment:v1 ls -la /nais" ;
                sh "sudo docker run --rm -v ${workspace}/config/basta:/nais navikt/deployment:v1 /app/deploy --apikey=${NAIS_DEPLOY_APIKEY} --cluster='dev-fss' --repository=${application} --resource='/nais/naiserator.yml' --vars='/nais/basta-dev-fss.json' --var='image=${dockerimage}' --wait=true --print-payload" ;
            }
        }

        stage("Ship it?") {
            timeout(time: 2, unit: 'DAYS') {
                def message = "\nreleased version: ${releaseVersion}\nbuild #: ${env.BUILD_URL}\nShip it? ${env.BUILD_URL}input\n"
                slackSend channel: '#nais-ci', message: "${env.JOB_NAME} completed successfully\n${message}", teamDomain: 'nav-it', tokenCredentialId: 'slack_fasit_frontend'
                input message: 'Deploy to prod? ', ok: 'Proceed', submitter: '0000-ga-aura'
            }
        }

        stage('Deploy prod') {
            withCredentials([string(credentialsId: 'NAIS_DEPLOY_APIKEY', variable: 'NAIS_DEPLOY_APIKEY')]) {
                sh "echo 'Deploying ${application}:${releaseVersion} to prod-fss'"
                sh "chown -R jenkins:jenkins ${workspace}"
                sh "sudo docker run --rm -v ${workspace}/config/basta:/nais navikt/deployment:v1 /app/deploy --apikey=${NAIS_DEPLOY_APIKEY} --cluster='prod-fss' --repository=${application} --resource='/nais/naiserator.yml' --vars='/nais/basta-prod-fss.json' --var='image=${dockerimage}' --wait=true --print-payload" ;
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
