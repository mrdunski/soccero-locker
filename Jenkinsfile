node ('java') {
    stage('test project') {
        checkout scm
        sh 'chmod +x ./gradlew'
        sh './gradlew clean test'
    }
    // junit '**/test-results/*.xml'
}

//node ('java') {
//    stage 'release'
//    checkout scm
//    sh './gradlew clean release pushRelease'
//}

node ('docker') {
    stage('build docker') {
        withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'password', usernameVariable: 'user')]) {
            sh "docker login -u $user -p $password"
            checkout scm
            sh 'chmod +x ./gradlew'
            sh './gradlew clean dockerBuildImage dockerPushImage'
        }
    }
}