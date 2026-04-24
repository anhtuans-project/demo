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
                                    # 1. Ép buộc dùng TLS 1.2
                                    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

                                    # 2. Bỏ qua lỗi SSL Certificate (chỉ dùng cho dev/test nếu gặp lỗi cert)
                                    add-type @"
                                        using System.Net;
                                        using System.Security.Cryptography.X509Certificates;
                                        public class TrustAllCertsPolicy : ICertificatePolicy {
                                            public bool CheckValidationResult(
                                                ServicePoint srvPoint, X509Certificate certificate,
                                                WebRequest request, int certificateProblem) {
                                                return true;
                                            }
                                        }
        "@
                                    [System.Net.ServicePointManager]::CertificatePolicy = New-Object TrustAllCertsPolicy

                                    # 3. Cấu hình URL và Header
                                    $url = "https://github.com/codacy/codacy-coverage-reporter/releases/download/12.0.0/codacy-coverage-reporter-12.0.0-assembly.jar"
                                    $outputFile = "codacy-reporter.jar"

                                    # Tạo header User-Agent để GitHub không chặn
                                    $header = @{
                                        "User-Agent" = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
                                    }

                                    Write-Host "Downloading reporter from GitHub..."
                                    Invoke-WebRequest -Uri $url -OutFile $outputFile -Headers $header -UseBasicParsing -TimeoutSec 30

                                    Write-Host "Download successful. Running reporter..."
                                    java -jar $outputFile report -l Java -r target/site/jacoco/jacoco.xml --api-token $env:CODACY_PROJECT_TOKEN

                                    Write-Host "✅ Codacy upload completed successfully."
                                '''
                            }
                        } catch (Exception e) {
                            echo "⚠️ WARNING: Failed to upload coverage to Codacy."
                            echo "Error: ${e.message}"
                            echo "This might be due to network restrictions, firewall, or GitHub rate limiting."
                            echo "Pipeline will continue without coverage report."
                            currentBuild.result = 'UNSTABLE' // Bỏ comment nếu muốn build màu vàng
                        }
                    } else {
                        echo "No JaCoCo coverage report found. Skipping."
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