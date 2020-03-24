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

The performance model apply the benchmarking. 

"benchmarks" sub-project includes an extension to RUBiS cloud benchmark web application.

The benchmark client is avaiable at performance-model/Benchmark Scripts. 
