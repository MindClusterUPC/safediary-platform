pipeline {
  agent any

  tools {
    maven 'MAVEN_3_9'
    jdk 'JDK_21'
  }

  environment {
    TEST_FILTER = '!*KarateTest'
  }

  stages {
    stage('Compile') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B clean compile'
        }
      }
    }

    stage('Unit Tests') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B test -Dtest="$TEST_FILTER"'
        }
      }
    }

    stage('Acceptance Tests') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B test -Dtest="*KarateTest"'
        }
      }
    }

    stage('Coverage') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B jacoco:report jacoco:check'
        }
      }
    }

    stage('Package') {
      steps {
        withMaven(maven: 'MAVEN_3_9', options: [junitPublisher(disabled: true)]) {
          sh 'mvn -B package -DskipTests'
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts artifacts: 'target/site/jacoco/**, target/karate-reports/**', allowEmptyArchive: true
    }
    success {
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    }
  }
}
