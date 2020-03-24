## refactoring-ml

This component implements machine-learning and rule-based (knowledge-based) refactoring of application deployments.  

## Prerequisites
This module depends on the SODALITE sub-projects "refactoring-option-discoverer" and “semantic-reasoner”. Thus, first built them.

"benchmarks" sub-project includes an extension to RUBiS cloud benchmark web application

## Rule-based Refactoring
"rule-based" sub-project includes the rule-based refactoring engine. To build it, you can use `maven`
```
mvn clean install
```
