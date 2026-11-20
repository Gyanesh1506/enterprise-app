pipeline {
    agent any

    environment {
        DB_URL = 'jdbc:postgresql://172.17.0.3:5432/enterprise_db'
        DB_USERNAME = 'appuser'
        DB_PASSWORD = credentials('db-password')
        SKIP_CI = 'false'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out Enterprise Employee App'
                checkout scm
            }
        }

        stage('Check Trigger') {
            steps {
                script {
                    def commitMessage = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()

                    if (commitMessage.contains('[skip ci]')) {
                        env.SKIP_CI = 'true'
                        echo 'GitOps-only commit detected. Skipping CI stages.'
                    }
                }
            }
        }

        stage('Build and Unit Tests') {
            when {
                expression { env.SKIP_CI != 'true' }
            }

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
            when {
                expression { env.SKIP_CI != 'true' }
            }

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
            when {
                expression { env.SKIP_CI != 'true' }
            }

            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Docker Build') {
            when {
                expression { env.SKIP_CI != 'true' }
            }

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

        stage('Docker Push') {
            when {
                expression { env.SKIP_CI != 'true' }
            }

            agent {
                label 'docker-agent'
            }

            steps {
                echo 'Pushing Docker image to Docker Hub'

                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKERHUB_USERNAME',
                    passwordVariable: 'DOCKERHUB_TOKEN'
                )]) {
                    sh '''
                        echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

                        docker push gyan1506/enterprise-app:${BUILD_NUMBER}

                        docker logout
                    '''
                }
            }
        }

        stage('Update GitOps Manifest') {
            when {
                expression { env.SKIP_CI != 'true' }
            }

            agent {
                label 'docker-agent'
            }

            steps {
                echo 'Updating Kubernetes deployment manifest'

                withCredentials([usernamePassword(
                    credentialsId: 'gitops-creds',
                    usernameVariable: 'GITOPS_USERNAME',
                    passwordVariable: 'GITOPS_TOKEN'
                )]) {
                    sh '''
                        set +x

                        cat > "$WORKSPACE/.git-askpass" <<'EOF'
#!/bin/sh
case "$1" in
    *Username*) printf '%s\\n' "$GITOPS_USERNAME" ;;
    *Password*) printf '%s\\n' "$GITOPS_TOKEN" ;;
    *) exit 1 ;;
esac
EOF

                        chmod 700 "$WORKSPACE/.git-askpass"

                        trap 'rm -f "$WORKSPACE/.git-askpass"' EXIT

                        export GIT_ASKPASS="$WORKSPACE/.git-askpass"
                        export GIT_TERMINAL_PROMPT=0

                        git config user.name "Jenkins CI"
                        git config user.email "jenkins-ci@users.noreply.github.com"

                        git fetch origin main
                        git rebase origin/main

                        sed -i -E "s#(image: gyan1506/enterprise-app:).*#\\1${BUILD_NUMBER}#" k8s/deployment.yaml

                        echo "Updated deployment image:"
                        grep 'image:' k8s/deployment.yaml

                        git diff --check

                        git add k8s/deployment.yaml

                        if git diff --cached --quiet; then
                            echo 'No deployment manifest changes detected.'
                        else
                            git commit -m "Update deployment image to ${BUILD_NUMBER} [skip ci]"

                            git push origin HEAD:main
                        fi
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'CI/CD preparation pipeline completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the failed stage.'
        }
    }
}
