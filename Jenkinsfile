node {
	def committer, committerEmail, changelog, releaseVersion // metadata
	def application = "basta"
	def mvnHome = tool "maven-3.3.9"
	def	mvn = "${mvnHome}/bin/mvn"
	def	npm = "/usr/bin/npm"
	def	node = "/usr/bin/node"
	def	gulp = "${node} ./node_modules/gulp/bin/gulp.js"
	def	protractor = "./node_modules/protractor/bin/protractor"
	def appConfig = "app-config.yaml"
  def dockerRepo = "docker.adeo.no:5000"
  def branch = "AURA-1999"
  def groupId = "nais"

	try {
		stage("checkout") {
			git url: "ssh://git@stash.devillo.no:7999/aura/${application}.git", branch: "${branch}"
		}

		stage("initialize") {
			def pom = readMavenPom file: 'pom.xml'
			releaseVersion = pom.version.tokenize("-")[0]
			changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)

			sh 'echo "Verifying that no snapshot dependencies is being used."'
            sh 'if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi'
		}

		stage("build and test application") {
		    withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
        					sh "${npm} install"
        					sh "${gulp} dist"
        				}

			sh "${mvn} clean install -Djava.io.tmpdir=/tmp/${application} -B -e"

			wrap([$class: 'Xvfb']) {
            					sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test &"
            					sh "sleep 20"
            					retry("3".toInteger()) {
            						sh "${protractor} ./src/test/js/protractor_config.js"
            					}
            					sh "pgrep -f StandaloneBastaJettyRunner | xargs -I% kill -9 %"
            				}
		}

		stage("release version") {
      sh "cp ${mvnHome}/conf/settings.xml ."
      sh "sudo docker build --build-arg version=${releaseVersion} --build-arg app_name=${application} -t ${dockerRepo}/${application}:${releaseVersion} ."
			sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
			sh "git push --tags"
		}

		stage("publish artifact") {
      sh "sudo docker push ${dockerRepo}/${application}:${releaseVersion}"
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'nexusUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
         sh "curl -s -F r=m2internal -F hasPom=false -F e=yaml -F g=${groupId} -F a=${application} -F v=${releaseVersion} -F p=yaml -F file=@${appConfig} -u ${env.USERNAME}:${env.PASSWORD} http://maven.adeo.no/nexus/service/local/artifact/maven/content"
                }
    	}
			
		stage("deploy to dev/test") {
      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
        	sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${releaseVersion}\", \"environment\": \"u1\", \"zone\": \"fss\", \"namespace\": \"default\", \"username\": \"${env.USERNAME}\", \"password\": \"${env.PASSWORD}\"}\' https://daemon.nais.devillo.no/deploy"
      }
		}
			
		stage("new dev version") {
			def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
			sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
			sh "git commit -m \"Updated to new dev-version ${nextVersion} after release by ${committer}\" pom.xml" 
			sh "git push origin ${branch}"
		}

		stage("jilease") {
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
			sh "/usr/bin/jilease -jiraUrl https://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
			}
		}

		stage("deploy to prod") {
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
			sh "curl -k -d \'{\"application\": \"${application}\", \"version\": \"${re    leaseVersion}\", \"environment\": \"p\", \"zone\": \"fss\", \"namespace\": \"defaul    t\", \"username\": \"${env.USERNAME}\", \"password\": \"${env.PASSWORD}\"}\' https:/    /daemon.nais.devillo.no/deploy"
      }
		}
		
		def message = "Successfully deployed ${application}:${releaseVersion} to prod\n${changelog}\nhttps://${application}.adeo.no"
        //hipchatSend color: 'GREEN', message: "${message}", textFormat: true, room: 'aura', v2enabled: true
	} catch (e) {
		currentBuild.result = "FAILED"
		def message = "${application} pipeline failed. See jenkins for more info ${env.BUILD_URL}\n${changelog}"
    	//hipchatSend color: 'RED', message: "@all ${env.JOB_NAME} failed\n${message}", textFormat: true, notify: true, room: 'AuraInternal', v2enabled: true
		throw e
	}
}
