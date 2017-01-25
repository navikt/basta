node {
	def committer, committerEmail, changelog, releaseVersion // metadata
	def application = "basta"
	def mvnHome = tool "maven-3.3.9"
	def	mvn = "${mvnHome}/bin/mvn"
	def	nodeHome = tool "nodejs-6.6.0"
	def	npm = "${nodeHome}/bin/npm"
	def	node = "${nodeHome}/bin/node"
	def	gulp = "${node} ./node_modules/gulp/bin/gulp.js"
	def	protractor = "./node_modules/protractor/bin/protractor"
	
	try {
		stage("checkout") {
			git url: "ssh://git@stash.devillo.no:7999/aura/${application}.git"
		}

		stage("initialize") {
			
			def pom = readMavenPom file: 'pom.xml'
			releaseVersion = pom.version.tokenize("-")[0]
			committer = sh(script: 'git log -1 --pretty=format:"%ae (%an)"', returnStdout: true).trim()
			committerEmail = sh(script: 'git log -1 --pretty=format:"%ae"', returnStdout: true).trim()
			changelog = sh(script: 'git log `git describe --tags --abbrev=0`..HEAD --oneline', returnStdout: true)
		}

		stage("verify maven versions") {
			// aborts pipeline if releaseVersion already is released
			sh "if [ \$(curl -s -o /dev/null -I -w \"%{http_code}\" http://maven.adeo.no/m2internal/no/nav/aura/${application}/${application}-appconfig/${releaseVersion}) != 404 ]; then echo \"this version is somehow already released, manually update to a unreleased SNAPSHOT version\"; exit 1; fi"

			// no snapshots dependencies when creating a release
			sh 'echo "Verifying that no snapshot dependencies is being used."'
			sh 'grep module pom.xml | cut -d">" -f2 | cut -d"<" -f1 > snapshots.txt'
			sh 'echo "./" >> snapshots.txt'
			sh 'while read line;do if [ "$line" != "" ];then if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi;fi;done < snapshots.txt'
		}

		stage("build and test frontend") {
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

		stage("browsertest") {
			wrap([$class: 'Xvfb']) {
				dir("war") {
					sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test &"
					sh "sleep 20"
					retry("3".toInteger()) {
						sh "${protractor} ./src/test/js/protractor_config.js"
					}
					sh "pgrep -f StandaloneBastaJettyRunner | xargs -I% kill -9 %"
				}
			}
		}

		stage("release version") {
			sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
			sh "git commit -am \"set version to ${releaseVersion} (from Jenkins pipeline)\""
			sh "git push origin master"
			sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
			sh "git push --tags"
		}

		stage("publish artifact") {
			sh "${mvn} clean deploy -DskipTests -B -e"
		}
			
		stage("deploy to test") {
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
				sh "${mvn} aura:deploy -Dapps=${application}:${releaseVersion} -Denv=u1 -Dusername=${env.USERNAME} -Dpassword=${env.PASSWORD} -Dorg.slf4j.simpleLogger.log.no.nav=debug -B -Ddebug=true -e"
			}
		}
			
		stage("new dev version") {
			def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
			sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
			sh "git commit -am \"updated to new dev-version ${nextVersion} after release by ${committer}\""
			sh "git push origin master"
		}

		stage("jilease") {
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
				sh "/usr/bin/jilease -jiraUrl http://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
			}
		}

		stage("deploy to prod") {
			withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'srvauraautodeploy', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
				sh "${mvn} aura:deploy -Dapps=${application}:${releaseVersion} -Denv=p -Dusername=${env.USERNAME} -Dpassword=${env.PASSWORD} -Dorg.slf4j.simpleLogger.log.no.nav=debug -B -Ddebug=true -e"
			}
		}
		
		notifySuccessful()
		
	} catch (e) {
		currentBuild.result = "FAILED"
		notifyFailure()
		throw e
	}
}

def notifySuccessful() {
	def emailBody = "${application}:${releaseVersion} now in production. See jenkins for more info ${env.BUILD_URL}\n${changelog}"
	mail body: emailBody, from: "jenkins@aura.adeo.no", subject: "SUCCESSFULLY completed ${env.JOB_NAME}!", to: committerEmail
	def message = "Successfully deployed ${application}:${releaseVersion} to prod\n${changelog}\nhttps://${application}.adeo.no"

	hipchatSend color: 'GREEN', message: "${message}", textFormat: true, room: 'Aura - Automatisering', v2enabled: true
}

def notifyFailure() {
	def emailBody = "AIAIAI! Your last commit on ${application} didn't go through. See log for more info ${env.BUILD_URL}\n${changelog}"
	mail body: emailBody, from: "jenkins@aura.adeo.no", subject: "FAILED to complete ${env.JOB_NAME}", to: committerEmail
	def message = "${application} pipeline failed. See jenkins for more info ${env.BUILD_URL}\n${changelog}"

	hipchatSend color: 'RED', message: "@all ${env.JOB_NAME} failed\n${message}", textFormat: true, notify: true, room: 'AuraInternal', v2enabled: true
}
