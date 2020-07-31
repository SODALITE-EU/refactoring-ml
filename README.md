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
# Rule-based Refactoring API
```
sudo docker build -t sodalite/rule-based-refactorer .
sudo docker run -p 8080:8080 -d --name=rule-based-refactorer sodalite/rule-based-refactorer
sudo docker start rule-based-refactorer
sudo docker logs rule-based-refactorer
sudo docker stop rule-based-refactorer
sudo docker rm  rule-based-refactorer
sudo docker rmi sodalite/rule-based-refactorer
```
# Performance Prediction API
```
sudo docker build -t sodalite/performance-predictor-refactoring .
sudo docker run -p 5000:5000 -d --name=perPredictAPI sodalite/performance-predictor-refactoring
sudo docker start perPredictAPI
sudo docker logs perPredictAPI
sudo docker stop perPredictAPI
sudo docker rm  perPredictAPI
sudo docker rmi sodalite/performance-predictor-refactoring
```

## Run Docker Compose
```
sudo docker-compose up
sudo docker image ls
```

## REST API
# To send custom deployment events 
```
POST http://{ip}:8080/rule-based-refactorer/v0.1/api/events
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
# To send Prometheus alerts
```
POST http://{ip}:8080/rule-based-refactorer/v0.1/api/alerts
```
Sample Requests

```
{
   "receiver": "webhook",
  "status": "firing",
  "alerts": [
    {
      "status": "firing",
      "labels": {
        "alertname": "Test",
        "dc": "eu-west-1",
        "instance": "localhost:9090",
        "job": "prometheus24"
      },
      "annotations": {
        "description": "some description"
      },
      "startsAt": "2018-08-03T09:52:26.739266876+02:00",
      "endsAt": "0001-01-01T00:00:00Z",
      "generatorURL": "http://somestats_alloc_bytes+%3E+0\u0026g0.tab=1"                                                                                  
    }
  ],
  "groupLabels": {
    "alertname": "Test",
    "job": "prometheus24"
  },
  "commonLabels": {
    "alertname": "Test",
    "dc": "eu-west-1",
    "instance": "localhost:9090",
    "job": "prometheus24"
  },
  "commonAnnotations": {
    "description": "some description"
  },
  "externalURL": "http://simon-laptop:9093",
  "version": "4",
  "groupKey": "{}:{alertname=\"Test\", job=\"prometheus24\"}"
}

```
# To send enable/disable pull-based monitoring. By default, the monitoring is disabled 
```
POST http://{ip}:8080/rule-based-refactorer/v0.1/api/monitoring/pull?state=enabled
```

# To update or replace the refactoring rules

Send the rules (.drl) file as multipart/form-data (name:” file”, value: actual file)
```
PUT http://localhost:8080/rule-based-refactorer/v0.1/api/rules
```
# To update or replace the information about blueprint  variants and input file

```
POST http://localhost:8080/rule-based-refactorer/v0.1/api/variants
```
A sample request

```
{
  "input": "... content of the input.yaml...",
  "blueprints": [
    {
      "target": [
        "de",
        "at"
      ],
      "bptoken": "3d6dc7a8-6cfa-4675-ab12-e6b0d0a4c9e0"
    },
    {
      "target": [
        "it"
      ],
      "bptoken": "3d6dc7a8-6cfa-4675-ab12-e6b0d0a4c9e0"
    }
  ]
}
```
# Performance Predictor API

## To create the train-data table

```
POST http://ip:5000/per-predictor/dtr/features
```
A sample request

```
{
  "features": [
    "workload",
    "D1",
    "D3",
    "D4",
    "D5",
    "D6",
    "D7",
    "D8",
    "D9",
    "D10",
    "D12",
    "D14",
    "D15",
    "D16"
  ],
  "metrics": [
    "mean_rt"
  ]
}
```
## To update the train-data

```
PUT http://ip:5000/per-predictor/dtr/features
```
A sample request

```
[
  {
    "workload": 100,
    "D1": 1,
    "D3": 0,
    "D4": 0,
    "D5": 0,
    "D6": 0,
    "D7": 1,
    "D8": 0,
    "D9": 0,
    "D10": 0,
    "D12": 0,
    "D14": 1,
    "D15": 0,
    "D16": 0,
    "mean_rt": 511
  },
  {
    "workload": 2000,
    "D1": 1,
    "D3": 0,
    "D4": 0,
    "D5": 0,
    "D6": 0,
    "D7": 1,
    "D8": 0,
    "D9": 0,
    "D10": 0,
    "D12": 0,
    "D14": 1,
    "D15": 0,
    "D16": 0,
    "mean_rt": 5661
  },
  .....
]
```
## To train the predictor 

```
http://34.66.81.209:5000/per-predictor/<model_type>/train
```
model_type can be dtr, rtfr, or mlpnn

dtr - decision tree regression
rtfr - Random Forest Regression
mlpnn - multiple-layer perceptron neural network

## To predict the performance using a trained predictor 

```
POST http://ip:5000/per-predictor/<model_type>/predict
```
A sample request

```
[
	{
    "workload": 2000,
    "D1": 1,
    "D3": 1,
    "D4": 0,
    "D5": 1,
    "D6": 0,
    "D7": 0,
    "D8": 0,
    "D9": 1,
    "D10": 1,
    "D12": 0,
    "D14": 1,
    "D15": 0,
    "D16": 0
 }
]
```
## To delete the trainin dataset

```
DELETE http://ip:5000/per-predictor/<model_type>/features
```
