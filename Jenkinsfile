// Pipeline script for CI/CD with Jenkins and Jira
// by vietviet08
pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-account')
        DOCKER_REGISTRY = "vietquoc2408"
        DOCKER_IMAGE_NAME = "bottom-cv"
        DOCKER_TAG = "0.0.2"
        SSH_CREDENTIALS_ID = "sg-server"
        REMOTE_SERVER = "146.190.93.46"
        REMOTE_USER = "root"
        REPO_URL = "https://github.com/vku-k23/bottom-cv"
        JIRA_EMAIL = credentials('jira-email')
        JIRA_API_TOKEN = credentials('jira-api-token')
        JIRA_BASE_URL = "https://vku-k23.atlassian.net"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Extract Issue Key from Commit Message') {
            steps {
                script {
                    echo "${env.BRANCH_NAME}"

                    def commitMessage = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    def issueKeyMatcher = commitMessage =~ /(SCRUM-\d+)/

                    if (issueKeyMatcher) {
                        env.ISSUE_KEY = issueKeyMatcher[0][1]
                        echo "Found issue key in commit message: ${env.ISSUE_KEY}"
                    } else {
                        echo "No issue key found in commit message."
                        currentBuild.result = 'ABORTED'
                        error("No issue key found in commit message. Aborting pipeline.")
                    }
                }
            }
        }

        stage('Update Jira Issue to In Progress') {
             steps {
                script {
                    def transitionId = "21"

                    sh """
                        curl -u ${JIRA_EMAIL}:${JIRA_API_TOKEN} -X POST \
                        -H "Content-Type: application/json" \
                        --data '{
                            "transition": {
                                "id": "${transitionId}"
                            }
                        }' \
                        "${JIRA_BASE_URL}/rest/api/3/issue/${env.ISSUE_KEY}/transitions"
                    """
                    echo "Updated issue ${env.ISSUE_KEY} to In Progress."
                }
            }
            post {
                 always {
                     jiraSendBuildInfo site: 'vku-k23'
                 }
             }
        }

        stage('Build Docker Image') {
            when {
                branch 'prod'
            }
//             when {
//                 expression { env.BRANCH_NAME ==~ /(prod|docker)/ }
//                 anyOf {
//                     environment name: 'DEPLOY_TO', value: 'prod'
//                     environment name: 'DEPLOY_TO', value: 'docker'
//                 }
//             }
            steps {
                script {
                    sh "docker build -t ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG} ."
                }
            }
        }

        stage('Push Docker Image') {
//             when {
//                 expression { env.BRANCH_NAME ==~ /(prod|docker)/ }
//                 anyOf {
//                     environment name: 'DEPLOY_TO', value: 'prod'
//                     environment name: 'DEPLOY_TO', value: 'docker'
//                 }
//             }
            when {
                branch 'prod'
            }
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: 'dockerhub-account',
                        usernameVariable: 'DOCKERHUB_CREDENTIALS_USR',
                        passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW'
                    )]) {
                        sh """
                            echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin
                            docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}
                        """
                    }
                }
            }
        }

        stage('Deploy to Server') {
            when {
                branch 'prod'
            }
            steps {
                script {
                    sshagent(credentials: [SSH_CREDENTIALS_ID]) {
                        try {
                            sh """
                                ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_SERVER} << 'EOF'
                                docker stop ${DOCKER_IMAGE_NAME} || true
                                docker rm ${DOCKER_IMAGE_NAME} || true

                                docker images --format "{{.Repository}}:{{.ID}}" | awk -v img="${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}" '\$1 == img {print \$2}' | xargs -r docker rmi -f

                                docker pull ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}

                                mkdir -p project && cd project

                                docker-compose up -d
EOF
                            """
                            echo "Deployment successful!"
                            def transitionId = "31"

                            sh """
                                curl -u ${JIRA_EMAIL}:${JIRA_API_TOKEN} -X POST \
                                -H "Content-Type: application/json" \
                                --data '{
                                    "transition": {
                                        "id": "${transitionId}"
                                    }
                                }' \
                                "${JIRA_BASE_URL}/rest/api/3/issue/${env.ISSUE_KEY}/transitions"
                            """
                            echo "Updated issue ${env.ISSUE_KEY} to Done."
                        } catch (Exception e) {
                            echo "Deployment failed: ${e}"
                            currentBuild.result = 'FAILURE'
                            throw e
                        }
                    }
                }
            }
             post {
                 always {
                     jiraSendDeploymentInfo site: 'vku-k23', environmentId: 'stg-base', environmentName: 'stg-base', environmentType: 'staging'
                 }
             }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}