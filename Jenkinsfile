node {
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
        // time this and post to influx?
        sh "${mvn} clean test -B -e"
    }

    stage("test frontend") {
        wrap([$class: 'Xvfb']) {
            dir("war") {
                sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test &"
                sh "sleep 15"
                sh "${protractor} ./src/test/js/protractor_e2e_test.js"
            }
        }
    }
//
//    stage("tests") {
//        parallel(
//            "unit test": {
//                // time this and post to influx?
//                sh "${mvn} clean test -B -e"
//            },
//            "gui test": {
//                wrap([$class: 'Xvfb']) {
//                    dir("war") {
//                        sh "${mvn} exec:java -Dexec.mainClass=no.nav.aura.basta.StandaloneBastaJettyRunner -Dexec.classpathScope=test &"
//                        sh "sleep 15"
//                        sh "${protractor} ./src/test/js/protractor_e2e_test.js"
//                    }
//                }
//            }
//        )
//    }

//    stage("create version") {
//        sh "${mvn} versions:set -B -DnewVersion=${releaseVersion} -DgenerateBackupPoms=false"
//        sh "git commit -am \"set version to ${releaseVersion} (from Jenkins pipeline)\""
//        sh "git push origin master"
//        sh "git tag -a ${application}-${releaseVersion} -m ${application}-${releaseVersion}"
//        sh "git push --tags"
//    }
//
//    stage("publish artifact") {
//        sh "${mvn} clean deploy -DskipTests -B -e"
//    }
//
//    stage("jilease") {
//        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'jiraServiceUser', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
//            sh "/usr/bin/jilease -jiraUrl http://jira.adeo.no -project AURA -application ${application} -version $releaseVersion -username $env.USERNAME -password $env.PASSWORD"
//        }
//    }
//
//    stage("new dev version") {
//        def nextVersion = (releaseVersion.toInteger() + 1) + "-SNAPSHOT"
//        sh "${mvn} versions:set -B -DnewVersion=${nextVersion} -DgenerateBackupPoms=false"
//        sh "git commit -am \"updated to new dev-version ${nextVersion} after release by ${commiter}\""
//        sh "git push origin master"
//    }
}