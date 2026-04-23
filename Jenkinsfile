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
                           // Chỉ chạy nếu file coverage tồn tại
                                           if (fileExists('target/site/cobertura/coverage.xml')) {
                                               powershell '''
                                                   Invoke-WebRequest -Uri 'https://coverage.codacy.com/get.ps1' -OutFile 'codacy-reporter.ps1'

                                                   # ./codacy-reporter.ps1 -ProjectToken 'YOUR_CODACY_TOKEN' -CoverageReports 'target/site/cobertura/coverage.xml'

                                                   echo "Codacy script downloaded. Skipping upload due to missing token configuration."
                                               '''
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