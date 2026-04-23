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
                                                                           # Tải phiên bản mới nhất của reporter từ GitHub Releases
                                                                           \$url = "https://github.com/codacy/codacy-coverage-reporter/releases/download/12.0.0/codacy-coverage-reporter-12.0.0-assembly.jar"
                                                                           Invoke-WebRequest -Uri \$url -OutFile "codacy-reporter.jar"

                                                                           # Chạy file jar với Java (cần cài Java trong PATH)
                                                                           java -jar codacy-reporter.jar report -l Java -r target/site/jacoco/jacoco.xml --api-token ${env.CODACY_PROJECT_TOKEN}
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