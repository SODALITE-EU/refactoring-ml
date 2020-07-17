## refactoring-ml

This component implements machine-learning and rule-based (knowledge-based) refactoring of application deployments.  

## Prerequisites
This module depends on the SODALITE sub-projects "refactoring-option-discoverer" and “semantic-reasoner”. Thus, first built them.

The information about build processes can be found in the corresponding projects.

`https://github.com/SODALITE-EU/refactoring-option-discoverer`

`https://github.com/SODALITE-EU/semantic-reasoner`

## Rule-based Refactoring
"rule-based" sub-project includes the rule-based refactoring engine. To build it, you can use `maven`
```
mvn clean install
```
The built artifact is a web application (.war file) that can be deployed in an any web server. 

## ML-based Performance Modeling and Refactoring

The performance modeling applies the benchmarking (at design time) and machine learning. 

"benchmarks-apps" sub-project includes an extension to RUBiS cloud benchmark web application.

The benchmark client is available at performance-model/benchmark-clients. The data collected from Google Cloud and RuBIS is at performance-model/benchmarkdata.

## Docker Image Building and Usage
```
sudo docker build -t sodalite/rule-based-refactorer .
sudo docker run -p 5000:5000 -d --name=rule-based-refactorer sodalite/rule-based-refactorer
sudo docker start rule-based-refactorer
sudo docker logs rule-based-refactorer
sudo docker stop rule-based-refactorer
sudo docker rm  rule-based-refactorer
sudo docker rmi sodalite/rule-based-refactorer
```
## Run Docker Compose
```
sudo docker-compose up
sudo docker image ls
```

## REST API
```
http://{ serverIP}:8080/refactorer-api/v0.1/events/inputs
```
Sample Requests

```
{
	"new_location": "DE",
	"event_type" : "DeploymentNeeded"
}

{
	"new_location": "DE",
	"previous_location": "DE",
	"event_type" : "LocationChanged"
}
```
