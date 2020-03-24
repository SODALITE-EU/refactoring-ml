## refactoring-ml

This component implements machine-learning and rule-based (knowledge-based) refactoring of application deployments.  

## Prerequisites
This module depends on the SODALITE sub-projects "refactoring-option-discoverer" and “semantic-reasoner”. Thus, first built them.

## Rule-based Refactoring
"rule-based" sub-project includes the rule-based refactoring engine. To build it, you can use `maven`
```
mvn clean install
```
## Rule-based Refactoring

The performance model apply the benchmarking. 

"benchmarks" sub-project includes an extension to RUBiS cloud benchmark web application.

The benchmark client is avaiable at performance-model/Benchmark Scripts. 
