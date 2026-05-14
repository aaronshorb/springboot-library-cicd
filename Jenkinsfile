pipeline {
    agent any
    
    tools {
        maven 'M3'  
        jdk 'OpenJDK 21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                echo 'Checkout succeeded'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean compile'
                echo 'Build succeeded'
            }
        }

        stage('Dependency Scanning Parallel') {
            parallel {
                stage('OWASP Dependency-Check') {
                    steps {
                        withCredentials([string(credentialsId: 'nvd-api-key', variable: 'NVD_API_VAR')]){
                            sh '''mvn -B org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=9 -DnvdApiKey=$NVD_API_VAR -DdataDirectory="$WORKSPACE/.dc-data" -Dformats=HTML,XML'''
                            stash includes: 'target/dependency-check-report.xml,target/dependency-check-report.html', name: 'dependency-check-reports'
                        }
                    }
                }
                stage ('Maven Dependency Audit'){
                    steps {
                        sh 'mvn versions:display-dependency-updates'
                    }
                }
            }
        }

        stage('Publish Dependency-Check Results') {
            steps {
                unstash 'dependency-check-reports'
                dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
                publishHTML(
                    [allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'target',
                    reportFiles: 'dependency-check-report.html',
                    reportName: 'Dependency-Check Report',
                    reportTitles: '',
                    ])
            }
        }

        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                catchError (buildResult:'SUCCESS',stageResult:'UNSTABLE') {
                    sh 'mvn test -Pintegration-tests'
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                catchError (buildResult:'SUCCESS',stageResult:'UNSTABLE') {
                    sh 'mvn jacoco:report'
                }
            }
            post {
                always {
                    publishHTML(
                        [allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Coverage Report'
                        ])
                }
            }
        }

        stage('SAST - SonarQube') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_VAR')]){
                    sh '''mvn clean verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=Book-System-Project -Dsonar.projectName='Book-System-Project' -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml -Dsonar.java.binaries=target/classes -Dsonar.host.url=http://localhost:9000 -Dsonar.token=$SONAR_VAR'''
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t waternogetenemy/book-library:${BUILD_NUMBER} .'
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PW')]){
                    sh '''
                        echo "${DOCKER_PW}" | docker login -u "${DOCKER_USER}" --password-stdin
                        docker push waternogetenemy/book-library:${BUILD_NUMBER}
                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no docker-compose.yml ubuntu@13.59.18.63:/home/ubuntu/docker-compose.yml
                        ssh -o StrictHostKeyChecking=no ubuntu@13.59.18.63 "
                            export IMAGE_TAG=${BUILD_NUMBER}
                            docker compose -f /home/ubuntu/docker-compose.yml pull app
                            docker compose -f /home/ubuntu/docker-compose.yml up -d"
                    '''
                }
            }
        }

        stage('Validate Deployment') {
            steps {
                sleep 60
                sh 'curl --fail http://13.59.18.63:8080'
            }
        }

    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

