pipeline {
  agent any
  stages {
    stage('build') {
      steps {
        sh 'mvn clean compile'
      }
    }

    stage('test-linux') {
      steps {
        sh 'mvn test'
      }
    }

  }
}