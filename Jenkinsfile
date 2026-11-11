pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk 'jdk-21'
        nodejs 'node-24'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    environment {
        INTERNAL_TOKEN = 'pipeline-internal-token'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend build e testes') {
            steps {
                dir('backend') {
                    sh 'mvn -B -ntp verify'
                }
            }
        }

        stage('Worker build e testes') {
            steps {
                dir('worker') {
                    sh 'npm ci'
                    sh 'npm run build'
                    sh 'npm test'
                }
            }
        }

        stage('Frontend build e testes') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm test -- --watch=false'
                    sh 'npm run build'
                }
            }
        }

        stage('Imagens Docker') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose build'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'backend/target/surefire-reports/*.xml'
        }
        success {
            archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
        }
    }
}
