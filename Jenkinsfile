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
                            // Download and run the Codacy Coverage Reporter
                            powershell '''
                            curl -Ls https://coverage.codacy.com/get.powershell | bash -s report -r coverage/cobertura-coverage.xml
                            '''
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