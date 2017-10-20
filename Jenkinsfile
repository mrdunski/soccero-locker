node ('docker') {
    stage('test project') {
        checkout scm
        sh 'chmod +x ./gradlew'
        sh './gradlew clean test'
    }

    stage('build docker') {
        withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'password', usernameVariable: 'user')]) {
            sh "docker login -u $user -p $password"
            checkout scm
            sh 'chmod +x ./gradlew'
            sh "./gradlew dockerBuildImage dockerCustomPush generateK8sFile -Pv=$BUILD_NUMBER"
            archiveArtifacts 'build/soccero-locker.yaml'
            stash includes: 'build/soccero-locker.yaml', name: 'soccero-locker.yaml'
        }
    }
}

node ('kubectl') {
    stage('deploy') {
        checkout scm
        unstash 'soccero-locker.yaml'
        sh 'kubectl -n leanforge apply -f build/soccero-locker.yaml'
    }
}