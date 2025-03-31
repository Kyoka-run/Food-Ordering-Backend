pipeline {
    agent any
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
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
                withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'SSH_KEY')]) {
                    // Create a temporary PowerShell script to handle the SSH command
                    powershell '''
                        $keyPath = $env:SSH_KEY

                        # Create a simple SSH command to run the Docker commands
                        $sshCommand = "ssh -i `"$keyPath`" -o StrictHostKeyChecking=no ${env:EC2_USER}@${env:EC2_HOST} 'sudo docker login -u kyoka74022 -p Cinder1014 && sudo docker pull ${env:BACKEND_IMAGE} && sudo docker stop ${env:BACKEND_CONTAINER} || true && sudo docker rm ${env:BACKEND_CONTAINER} || true && sudo docker run -d -p 8080:8080 -e DB_HOST=${env:DB_HOST} -e DB_PORT=${env:DB_PORT} -e DB_NAME=${env:DB_NAME} -e DB_USERNAME=${env:DB_USERNAME} -e DB_PASSWORD=${env:DB_PASSWORD} --name ${env:BACKEND_CONTAINER} ${env:BACKEND_IMAGE}'"

                        # Execute the SSH command
                        Invoke-Expression $sshCommand

                        # Check if the command was successful
                        if ($LASTEXITCODE -ne 0) {
                            Write-Error "SSH command failed with exit code: $LASTEXITCODE"
                            exit 1
                        }
                    '''
                }
            }
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