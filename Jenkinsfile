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
                    if (fileExists('target/site/jacoco/jacoco.xml')) {
                        echo "Found JaCoCo report. Attempting to upload to Codacy..."

                        try {
                            withCredentials([string(credentialsId: 'CODACY_PROJECT_TOKEN', variable: 'CODACY_PROJECT_TOKEN')]) {
                                powershell '''
                                    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

                                    $url = "https://github.com/codacy/codacy-coverage-reporter/releases/download/12.0.0/codacy-coverage-reporter-12.0.0-assembly.jar"
                                    $outputFile = "codacy-reporter.jar"

                                    # Thêm User-Agent để tránh bị GitHub chặn bot
                                    $header = @{"User-Agent"="Mozilla/5.0"}

                                    Write-Host "Downloading reporter..."
                                    Invoke-WebRequest -Uri $url -OutFile $outputFile -Headers $header -UseBasicParsing -TimeoutSec 10

                                    Write-Host "Running reporter..."
                                    java -jar $outputFile report -l Java -r target/site/jacoco/jacoco.xml --api-token $env:CODACY_PROJECT_TOKEN

                                    Write-Host "✅ Upload success."
                                '''
                            }
                        } catch (Exception e) {
                            // QUAN TRỌNG: Bắt lỗi và chỉ in cảnh báo, không throw exception ra ngoài
                            echo "⚠️ WARNING: Could not connect to GitHub/Codacy."
                            echo "Reason: ${e.message}"
                            echo "Skipping coverage upload. Build will continue."

                            // Tùy chọn: Đánh dấu build là UNSTABLE (màu vàng) thay vì FAILURE (đỏ)
                            currentBuild.result = 'UNSTABLE'
                        }
                    } else {
                        echo "No JaCoCo report found. Skipping."
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