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
                git branch: 'main', url: 'https://github.com/anhtuans-project/demo.git'
            }
        }
        stage('Build') {
            parallel {
                stage('Java') {
                    steps {
                            sh 'mvn clean install'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh 'mvn test'
                }
            }
        }

        stage('Upload Coverage to Codacy') {
                    steps {
                        script {
                            // Download and run the Codacy Coverage Reporter
                            sh '''
                            curl -Ls https://coverage.codacy.com/get.sh | bash -s report -r coverage/cobertura-coverage.xml
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