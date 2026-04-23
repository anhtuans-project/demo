// This is the Jenkinsfile that will be used to build & test the project.
pipeline {
    agent any
    options {
        skipDefaultCheckout()
    }
    tools {
        maven "mvn"
    }


    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/anhtuans-project/demo.git'
            }
        }
        stage('Build') {
            parallel {
                stage('Java') {
                    steps {
                            powershell 'mvn clean install'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    powershell 'mvn test'
                }
            }
        }

        stage('Upload Coverage to Codacy') {
                    steps {
                        script {
                                           // Sửa đường dẫn thành jacoco.xml
                                           if (fileExists('target/site/jacoco/jacoco.xml')) {
                                               withCredentials([string(credentialsId: 'CODACY_PROJECT_TOKEN', variable: 'CODACY_PROJECT_TOKEN')]) {
                                                   powershell """
                                                       Invoke-WebRequest -Uri 'https://coverage.codacy.com/get.ps1' -OutFile 'codacy-reporter.ps1'

                                                       # Chú ý: Codacy hỗ trợ cả jacoco.xml
                                                       ./codacy-reporter.ps1 -ProjectToken '${env.CODACY_PROJECT_TOKEN}' -CoverageReports 'target/site/jacoco/jacoco.xml'
                                                   """
                                               }
                                           } else {
                                               echo "No coverage file found. Skipping Codacy upload."
                                           }
                        }
                    }
        }
    }
    post {
        success {
            // Actions after the build succeeds
            echo 'Build was successful!'
        }
        failure {
            // Actions after the build fails
            echo 'Build failed. Check logs.'
        }
    }
}