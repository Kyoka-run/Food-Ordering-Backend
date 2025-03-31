pipeline {
    agent any
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        EC2_SSH_KEY = 'C:\\codes\\My aws keypair.pem'
        EC2_USER = 'ubuntu'
        EC2_HOST = '54.171.153.174'
        BACKEND_IMAGE = "kyoka74022/food-ordering-backend:${BUILD_NUMBER}"
        BACKEND_CONTAINER = "food-ordering-backend"
        DB_HOST = 'database-food-ordering.ctmcuac0g16u.eu-west-1.rds.amazonaws.com'
        DB_PORT = '3306'
        DB_NAME = 'database-food-ordering'
        DB_USERNAME = 'root'
        DB_PASSWORD = 'Cinder1014'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven Project') {
            steps {
                bat 'mvn clean package -DskipTests'
            }
        }

//         stage('Run Tests') {
//             steps {
//                 bat 'mvn test'
//             }
//         }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t ${BACKEND_IMAGE} ."
            }
        }

        stage('Login to DockerHub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', passwordVariable: 'DOCKERHUB_PSW', usernameVariable: 'DOCKERHUB_USR')]) {
                    bat 'echo %DOCKERHUB_PSW%| docker login -u %DOCKERHUB_USR% --password-stdin'
                }
            }
        }

         stage('Push Docker Image') {
            steps {
                bat "docker push ${BACKEND_IMAGE}"
            }
        }

        stage('Deploy to EC2') {
            steps {
                // Install OpenSSH if needed
                bat 'where ssh > nul 2>&1 || echo "OpenSSH not found. Please install OpenSSH on the Jenkins server."'

                // Use SSH to deploy to EC2
                bat """
                    echo "Deploying to EC2..."

                    rem Fix permissions on SSH key
                    icacls "${EC2_SSH_KEY}" /inheritance:r
                    icacls "${EC2_SSH_KEY}" /grant:r "%USERNAME%:R"

                    rem Connect to EC2 and deploy
                    ssh -o StrictHostKeyChecking=no -i "${EC2_SSH_KEY}" ${EC2_USER}@${EC2_HOST} "sudo docker login -u kyoka74022 -p Cinder1014 && sudo docker pull ${BACKEND_IMAGE} && sudo docker stop ${BACKEND_CONTAINER} || true && sudo docker rm ${BACKEND_CONTAINER} || true && sudo docker run -d -p 8080:8080 -e DB_HOST=${DB_HOST} -e DB_PORT=${DB_PORT} -e DB_NAME=${DB_NAME} -e DB_USERNAME=${DB_USERNAME} -e DB_PASSWORD=${DB_PASSWORD} --name ${BACKEND_CONTAINER} ${BACKEND_IMAGE}"
                """
            }
        }

            post {
                always {
                    bat 'docker logout'
                }
                success {
                    echo 'Backend deployment successful!'
                }
                failure {
                    echo 'Backend deployment failed!'
                }
            }
        }