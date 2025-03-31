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
                    powershell """
                        # Create .ssh directory if it doesn't exist
                        if (!(Test-Path "\$env:USERPROFILE\\.ssh")) {
                            New-Item -Path "\$env:USERPROFILE\\.ssh" -ItemType Directory
                        }

                        # Copy the key to the standard SSH location
                        \$key = Get-Content "${SSH_KEY}"
                        Set-Content -Path "\$env:USERPROFILE\\.ssh\\id_rsa" -Value \$key

                        # Set proper permissions on the key file
                        icacls "\$env:USERPROFILE\\.ssh\\id_rsa" /inheritance:r
                        icacls "\$env:USERPROFILE\\.ssh\\id_rsa" /grant:r "\$env:USERNAME:(R)"

                        # Deploy using SSH
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} "sudo docker login -u kyoka74022 -p Cinder1014 && sudo docker pull ${BACKEND_IMAGE} && sudo docker stop ${BACKEND_CONTAINER} || true && sudo docker rm ${BACKEND_CONTAINER} || true && sudo docker run -d -p 8080:8080 -e DB_HOST=${DB_HOST} -e DB_PORT=${DB_PORT} -e DB_NAME=${DB_NAME} -e DB_USERNAME=${DB_USERNAME} -e DB_PASSWORD=${DB_PASSWORD} --name ${BACKEND_CONTAINER} ${BACKEND_IMAGE}"

                        # Clean up by removing the key
                        Remove-Item "\$env:USERPROFILE\\.ssh\\id_rsa" -Force
                    """
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