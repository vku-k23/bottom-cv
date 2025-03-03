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
        DOCKER_COMPOSE_FILE = "docker-compose.yml"
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
                         sh ""
                            docker build -t ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG} .
                         ""
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
                            sh """
                                ssh -o StrictHostKeyChecking=no ${REMOTE_USER}@${REMOTE_SERVER} << 'EOF'
                                docker pull ${DOCKER_REGISTRY}/${DOCKER_IMAGE_NAME}:${DOCKER_TAG}

                                docker-compose -f ${DOCKER_COMPOSE_FILE} down

                                docker-compose -f ${DOCKER_COMPOSE_FILE} up -d
                                EOF
                            """
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
