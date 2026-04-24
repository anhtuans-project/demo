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
                    // Kiểm tra file coverage tồn tại
                    if (fileExists('target/site/jacoco/jacoco.xml')) {
                        echo "Found JaCoCo report. Uploading to Codacy..."

                        // Dùng withCredentials để inject token an toàn vào môi trường PowerShell
                        withCredentials([string(credentialsId: 'CODACY_PROJECT_TOKEN', variable: 'CODACY_PROJECT_TOKEN')]) {
                            powershell '''
                                # 1. Ép PowerShell dùng TLS 1.2 để tương thích với GitHub
                                [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

                                # 2. Tải file reporter từ GitHub
                                $url = "https://github.com/codacy/codacy-coverage-reporter/releases/download/12.0.0/codacy-coverage-reporter-12.0.0-assembly.jar"
                                $outputFile = "codacy-reporter.jar"

                                try {
                                    Write-Host "Downloading Codacy reporter from: $url"
                                    Invoke-WebRequest -Uri $url -OutFile $outputFile -UseBasicParsing
                                    Write-Host "Download successful."
                                } catch {
                                    Write-Error "Failed to download reporter via PowerShell. Error: $_"
                                    exit 1
                                }

                                # 3. Chạy reporter với token lấy từ biến môi trường (không nội suy trong Groovy)
                                # Lưu ý: Trong PowerShell, biến môi trường truy cập qua $env:TEN_BIEN
                                Write-Host "Running Codacy reporter..."
                                java -jar $outputFile report -l Java -r target/site/jacoco/jacoco.xml --api-token $env:CODACY_PROJECT_TOKEN

                                Write-Host "Upload completed."
                            '''
                        }
                    } else {
                        echo "No JaCoCo coverage report found at target/site/jacoco/jacoco.xml. Skipping upload."
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