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
                           if (fileExists('target/site/cobertura/coverage.xml')) {
                                           // Dùng withCredentials để bảo mật token
                                           withCredentials([string(credentialsId: 'CODACY_TOKEN', variable: 'CODACY_PROJECT_TOKEN')]) {
                                               powershell """
                                                   Invoke-WebRequest -Uri 'https://coverage.codacy.com/get.ps1' -OutFile 'codacy-reporter.ps1'

                                                   # Chạy script với token và file coverage
                                                   ./codacy-reporter.ps1 -ProjectToken '${env.CODACY_PROJECT_TOKEN}' -CoverageReports 'target/site/cobertura/coverage.xml'
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