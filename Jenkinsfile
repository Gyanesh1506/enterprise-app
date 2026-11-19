
pipeline {
    agent any

    environment {
        DB_URL = 'jdbc:postgresql://172.17.0.3:5432/enterprise_db'
        DB_USERNAME = 'appuser'
        DB_PASSWORD = credentials('db-password')
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out Enterprise Employee App'
                checkout scm
            }
        }

        stage('Build and Unit Tests') {
            steps {
                echo 'Building application and running unit tests'
                sh './mvnw -B clean verify'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: true
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        ./mvnw -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.9.0.7382:sonar \
                        -Dsonar.projectKey=enterprise-app \
                        -Dsonar.projectName="Enterprise Employee App" \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }

    post {
        success {
            echo 'CI Pipeline completed successfully!'
        }
        failure {
            echo 'CI Pipeline failed. Check the failed stage.'
        }
    }
}
