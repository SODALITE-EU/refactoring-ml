# Performance Model for Heterogenous Configurable Cloud Applications

This project consists of the Performance Models generated in Python for the master's thesis of the author in Data Science & Entrepreneurship.
The project consists of a Random Forest Regression, Decision Tree Regression, and Feedforward Multilayer Perceptron Regression models trained with parameter tuning GridSearch for each respective model.
The `benchmarkdata` consists of performance data (Response Time) of a configurable cloud system in GCP benchmarked.
The benchmark scripts are written in Python to run scheduled workloads simulated through Locust.io.

Steps to run the project:
* set the paths to the data in file `Dataset_merge_RUN_FIRST.py.`
* run the entire file first
* then set the path to the `results/final_data.csv` for the selected model
* run the selected model


