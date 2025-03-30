pipeline {
    agent any
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-credentials')
        BACKEND_IMAGE = "kyoka74022/food-ordering-backend:${BUILD_NUMBER}"
        BACKEND_CONTAINER = "food-ordering-backend"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build Maven Project') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
//         stage('Run Tests') {
//             steps {
//                 sh 'mvn test'
//             }
//         }
        
        stage('Build Docker Image') {
            steps {
                sh "docker build -t ${BACKEND_IMAGE} ."
            }
        }
        
        stage('Login to DockerHub') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }
        
        stage('Push Docker Image') {
            steps {
                sh "docker push ${BACKEND_IMAGE}"
            }
        }
        
        stage('Deploy Container') {
            steps {
                sh """
                    docker stop ${BACKEND_CONTAINER} || true
                    docker rm ${BACKEND_CONTAINER} || true
                    docker run -d -p 8080:8080 \
                    -e DB_HOST=${DB_HOST} \
                    -e DB_PORT=${DB_PORT} \
                    -e DB_NAME=${DB_NAME} \
                    -e DB_USERNAME=${DB_USERNAME} \
                    -e DB_PASSWORD=${DB_PASSWORD} \
                    --name ${BACKEND_CONTAINER} \
                    ${BACKEND_IMAGE}
                """
            }
        }
    }
    
    post {
        always {
            sh 'docker logout'
        }
        success {
            echo 'Backend deployment successful!'
        }
        failure {
            echo 'Backend deployment failed!'
        }
    }
}