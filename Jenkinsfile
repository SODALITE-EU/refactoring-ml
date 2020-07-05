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
    stage ('Build rule-based refactorer') {
      steps {
        sh  """ #!/bin/bash
                cd rule-based
                mvn clean install
            """
        archiveArtifacts artifacts: '**/*.war, **/*.jar', onlyIfSuccessful: true
      }
    }
	stage('Build docker images') {
            steps {
                sh "cd rule-based; docker build -t rule_based_refactorer -f Dockerfile ."                
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
