// This is a declarative pipeline script to build a Docker image, push it to Docker Hub, and deploy it to a remote server using SSH.
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
    }
     stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                         sh """docker build -t ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG} ."""
                    }
                }
            }

            stage('Push Docker Image') {
                steps {
                    script {
                         withCredentials([usernamePassword(credentialsId: 'dockerhub-account', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW')]) {
                            sh "echo ${DOCKERHUB_CREDENTIALS_PSW} | docker login -u ${DOCKERHUB_CREDENTIALS_USR} --password-stdin"
                        }
                        sh "docker push ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}"
                    }
                }
            }

            stage('Deploy to Server') {
                steps {
                    script {
                        sshagent(credentials: [SSH_CREDENTIALS_ID]) {
                            try {
                                sh """
                                    ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_SERVER} << 'EOF'

                                    docker stop ${DOCKER_IMAGE_NAME} || true
                                    docker rm ${DOCKER_IMAGE_NAME} || true

                                    docker images --format "{{.Repository}}:{{.ID}}" | awk -v img="${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}" '$1 == img {print $2}' | xargs -r docker rmi -f

                                    docker pull ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}

                                    mkdir -p project && cd project

                                    docker-compose up -d
EOF
                                """
                            } catch (Exception e) {
                                echo "Deployment failed: ${e}"
                                currentBuild.result = 'FAILURE'
                                throw e
                            }
                        }
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
