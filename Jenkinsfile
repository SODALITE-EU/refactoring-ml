pipeline {
  options { disableConcurrentBuilds() }
  agent { label 'docker-slave' }
  stages {
    stage ('Pull repo code from github') {
      steps {
        checkout scm
      }
    }
    stage ('Build refactoring-option-discoverer') {
      when { 
          not { 
                triggeredBy 'UpstreamCause' 
          }
      }
      steps {
        build 'refactoring-option-discoverer/master'
      }
    }
	stage('Test perf-predictor-api') {
        steps {
            sh  """ #!/bin/bash
			        cd perf-predictor-api
					python3 -mvenv .venv
					. .venv/bin/activate
					python3 -m pip install --upgrade pip
					python3 -m pip install -r requirements.txt			        
                    python3 -m pytest --pyargs -s ./tests --junitxml="results.xml" --cov=mlalgo --cov-report xml tests/
					cp *.xml $WORKSPACE
                """
            junit 'results.xml'
        }
    }
	stage('Test forecast-api') {
        steps {
            sh  """ #!/bin/bash
			        cd forecast-api
					python3 -mvenv .venv
					. .venv/bin/activate
					python3 -m pip install --upgrade pip
					python3 -m pip install -r requirements.txt			        
                    python3 -m pytest --pyargs -s ./tests --junitxml="results.xml" --cov=mlalgo --cov-report xml tests/
					cp *.xml $WORKSPACE
                """
            junit 'results.xml'
        }
    }
    stage ('Build rule-based refactorer') {
      steps {
        sh  """ #!/bin/bash
                cd rule-based
                mvn clean install
            """
      }
    }
	
	stage('SonarQube analysis perf-predictor-api'){
        environment {
          scannerHome = tool 'SonarQubeScanner'
        }
        steps {
            withSonarQubeEnv('SonarCloud') {
                sh  """ #!/bin/bash
                        cd "perf-predictor-api"
                        ${scannerHome}/bin/sonar-scanner
                    """
            }
        }
    }
	stage('SonarQube analysis forecast-api'){
        environment {
          scannerHome = tool 'SonarQubeScanner'
        }
        steps {
            withSonarQubeEnv('SonarCloud') {
                sh  """ #!/bin/bash
                        cd "forecast-api"
                        ${scannerHome}/bin/sonar-scanner
                    """
            }
        }
    }
	stage('SonarQube analysis'){
        environment {
          scannerHome = tool 'SonarQubeScanner'
        }
        steps {
            withSonarQubeEnv('SonarCloud') {
                sh  """ #!/bin/bash
                        cd "rule-based"
                        ${scannerHome}/bin/sonar-scanner
                    """
            }
        }
    }
	stage('Build docker images') {
            steps {
                sh "cd rule-based; docker build -t rule_based_refactorer -f Dockerfile ."   
		        sh "cd perf-predictor-api; docker build -t fo_perf_predictor_api -f Dockerfile ."
				sh "cd forecast-api; docker build -t forecast-api -f Dockerfile ."
            }
    }   
    stage('Push Dockerfile to DockerHub') {
            when {
               branch "master"
            }
            steps {
                withDockerRegistry(credentialsId: 'jenkins-sodalite.docker_token', url: '') {
                    sh  """#!/bin/bash                       
                            docker tag rule_based_refactorer sodaliteh2020/rule_based_refactorer:${BUILD_NUMBER}
                            docker tag rule_based_refactorer sodaliteh2020/rule_based_refactorer
                            docker push sodaliteh2020/rule_based_refactorer:${BUILD_NUMBER}
                            docker push sodaliteh2020/rule_based_refactorer
			                docker tag fo_perf_predictor_api sodaliteh2020/fo_perf_predictor_api:${BUILD_NUMBER}
                            docker tag fo_perf_predictor_api sodaliteh2020/fo_perf_predictor_api
                            docker push sodaliteh2020/fo_perf_predictor_api:${BUILD_NUMBER}
                            docker push sodaliteh2020/fo_perf_predictor_api
							docker tag forecast-api sodaliteh2020/forecast-api:${BUILD_NUMBER}
                            docker tag forecast-api sodaliteh2020/forecast-api
                            docker push sodaliteh2020/forecast-api:${BUILD_NUMBER}
                            docker push sodaliteh2020/forecast-api
                        """
                }
            }
    }
  }
  post {
    failure {
        slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }
    fixed {
        slackSend (color: '#6d3be3', message: "FIXED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})") 
    }
  }
}
