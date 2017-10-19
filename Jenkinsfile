node ('docker') {
    stage('test project') {
        sh 'chmod +x ./gradlew'
        sh './gradlew clean test'
    }

    stage('mark release') {
        withCredentials([usernamePassword(credentialsId: 'github', passwordVariable: 'password', usernameVariable: 'user')]) {
            sh "./gradlew release pushRelease -Prelease.customUsername=${user} -Prelease.customPassword=${password}"
        }
    }

    stage('build docker') {
        withCredentials([usernamePassword(credentialsId: 'docker', passwordVariable: 'password', usernameVariable: 'user')]) {
            sh "docker login -u $user -p $password"
            sh 'chmod +x ./gradlew'
            sh "./gradlew dockerBuildImage dockerCustomPush generateK8sFile"
            archiveArtifacts 'build/soccero-locker.yaml'
            stash includes: 'build/soccero-locker.yaml', name: 'soccero-locker.yaml'
        }
    }
}

node ('kubectl') {
    stage('deploy') {
        unstash 'soccero-locker.yaml'
        sh 'kubectl -n leanforge apply -f build/soccero-locker.yaml'
    }
}