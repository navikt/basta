node {
    def mvnHome = tool "maven-3.3.9"
//    def auraDeployCmd = "${mvnHome}/bin/mvn clean aura:deploy -Dorg.slf4j.simpleLogger.log.no.nav=debug"
    def application = "basta"
    def commiter
    def releaseVersion

    stage('checkout') {
        git url: "ssh://git@stash.devillo.no:7999/aura/${application}.git"
    }

    stage('gather metadata') {
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

    wrap([$class: 'Xvfb']) {
        stage('build and test (w/GUI)') {
            sh "${mvnHome}/bin/mvn clean install -Pit"
        }
    }

//    stage('build and test') {
//        withEnv(['HTTP_PROXY=http://webproxy-utvikler.nav.no:8088', 'NO_PROXY=adeo.no']) {
//            sh "${mvnHome}/bin/mvn clean install -B"
//        }
//    }

    stage('release version') {
//        sh "${mvnHome}/bin/mvn versions:set -B -DnewVersion=$releaseVersion -DgenerateBackupPoms=false"
//        sh "git commit -am 'Commit before creating tag ${application}-$releaseVersion, by $commiter'"
        println "setting version to ${releaseVersion}"
    }
//
//   stage('mvn deploy, git tag') {
//      sh "${mvnHome}/bin/mvn deploy scm:tag -B -Dphantomjs.binary.path=/opt/phantomjs/bin/phantomjs -DskipTests -Prelease"
//   }
//
//   stage('jilease') {
//      withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
//         sh "/usr/bin/jilease -jiraUrl http://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
//      }
//   }
//
//   stage('new dev version') {
//      sh "${mvnHome}/bin/mvn versions:set -B -DnewVersion=$nextVersion -DgenerateBackupPoms=false"
//      sh "git commit -am 'Updated version after release by  $commiter'"
//      sh "git push origin master"
//   }
//
//   stage('Deploy fasit to cd-u1') {
//      sh "$auraDeployCmd -Dapps=fasit:$releaseVersion -Denv=cd-u1 -Dusername=admin -Dpassword=admin -Ddebug=true -e"
//   }
//
//   stage('Deploy fasit to u1') {
//      sh "$auraDeployCmd -Dapps=fasit:$releaseVersion -Denv=u1 -Dusername=admin -Dpassword=admin"
//   }
//
//   stage('Integration tests') {
//      parallel(
//         'WasIntegrationTest': {
//            sh "$auraDeployCmd -Dapps=wasdeploy-test:RELEASE -Denv=cd-u1 -Dusername=admin -Dpassword=admin -DenvConfigUrl=$fasitItUrl -Ddebug=true -e"
//         },
//         'JbossIntegrationTest': {
//             sh "$auraDeployCmd -Dapps=autodeploy-test:RELEASE -Denv=cd-u1 -Dusername=admin -Dpassword=admin -DenvConfigUrl=$fasitItUrl -Ddebug=true -e"
//         }
//      )
//   }
}