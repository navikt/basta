node {
    def mvnHome = tool "maven-3.3.9"
    def application = "basta"
    def commiter
    def releaseVersion

    stage('checkout') {
        git url: "ssh://git@stash.devillo.no:7999/aura/${application}.git"
    }

    stage('initialize metadata') {
        def pom = readMavenPom file: 'pom.xml'
        releaseVersion = pom.version.tokenize("-")[0]
        sh 'git log -1 --pretty=format:"%ae (%an)" > commiter.txt'
        commiter = readFile("commiter.txt")
    }

    stage('check for SNAPSHOT dependencies') {
        sh 'echo "Verifying that no snapshot dependencies is being used."'
        sh 'grep module pom.xml | cut -d">" -f2 | cut -d"<" -f1 > snapshots.txt'
        sh 'echo "./" >> snapshots.txt'
        sh 'while read line;do if [ "$line" != "" ];then if [ `grep SNAPSHOT $line/pom.xml | wc -l` -gt 1 ];then echo "SNAPSHOT-dependencies found. See file $line/pom.xml.";exit 1;fi;fi;done < snapshots.txt'
    }

    stage('set release version') {
        sh "${mvnHome}/bin/mvn versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
        sh "git commit -am \"set version to ${releaseVersion} (from Jenkins pipeline)\""
    }

    wrap([$class: 'Xvfb']) {
        stage('build and test (w/GUI)') {
            sh "${mvnHome}/bin/mvn clean deploy -Pit"
        }
    }

    stage('create tag') {
        sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
        sh "git push --tags"
    }

    stage('jilease') {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
            sh "/usr/bin/jilease -jiraUrl http://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
        }
    }

    stage('new dev version') {
        def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
        sh "${mvnHome}/bin/mvn versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
        sh "git commit -am \"updated to new dev-version ${nextVersion} after release by ${commiter}\""
        sh "git push origin master"
    }
}