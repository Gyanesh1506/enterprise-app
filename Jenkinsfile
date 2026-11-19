
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

                stash name: 'app-jar',
                      includes: 'target/enterprise-app-0.0.1-SNAPSHOT.jar'
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

        stage('Docker Build') {
            agent {
                label 'docker-agent'
            }
            steps {
                echo 'Building Docker image on dedicated Docker Agent'

                unstash 'app-jar'

                sh 'docker build -t gyan1506/enterprise-app:${BUILD_NUMBER} .'

                sh 'docker images gyan1506/enterprise-app:${BUILD_NUMBER}'
            }
        }

        stage('Trivy Security Scan') {
            agent {
                label 'docker-agent'
            }
            steps {
                echo 'Scanning Docker image for HIGH and CRITICAL vulnerabilities'

                sh '''
                    docker run --rm \
                        -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy:latest image \
                        --scanners vuln \
                        --severity HIGH,CRITICAL \
                        --exit-code 0 \
                        gyan1506/enterprise-app:${BUILD_NUMBER}
                '''
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
