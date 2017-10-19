node ('java') {
    stage 'test project'
    checkout scm
    sh './gradlew clean test'
    // junit '**/test-results/*.xml'
}

node ('java') {
    stage 'release'
    checkout scm
    sh './gradlew clean release pushRelease'
}

node ('java') {
    stage 'assemble'
    checkout scm
    sh './gradlew clean dockerBuildImage dockerPushImage'
}