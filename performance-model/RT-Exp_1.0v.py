import pandas as pd
import numpy as np
import matplotlib.pyplot as plt 

from sklearn.tree import DecisionTreeRegressor
from sklearn import linear_model

dataset =  pd.read_csv('E:/JADS/Work/Data/RT-Dummy Data.csv')
dataset = dataset.drop(columns=['response_time', 'mean(X)', 'STD(s)','studentized_residual'])



plt.scatter(dataset['Normalized MG2'], dataset['Workload (RPs)'], color='red')
plt.title('MG2 Vs Workload', fontsize=14)
plt.xlabel('Impact (MG2)', fontsize=14)
plt.ylabel('Workload', fontsize=14)
plt.grid(True)
plt.show()



plt.scatter(dataset['Normalized MG2'], dataset['F1'], color='green')
plt.title('MG2 Vs F1', fontsize=14)
plt.xlabel('Impact (MG2)', fontsize=14)
plt.ylabel('F1', fontsize=14)
plt.grid(True)
plt.show()


### Train-Test data
X = dataset.iloc[:, 0:6].values
y = dataset.iloc[:, 6].values


# #### Multiple Linear Regression

mlregr = linear_model.LinearRegression()
mlregr.fit(X, y)

print('Intercept: \n', mlregr.intercept_)
print('Coefficients: \n', mlregr.coef_)

pred_values = [0,1,0,0,1,125]

print ('Predicted Impact MLR: \n', mlregr.predict([pred_values]))


#### Decision Tree Regressor
regressor = DecisionTreeRegressor(random_state=0)
regressor.fit(X,y)
print ('Predicted Impact DTR: \n', regressor.predict([pred_values]))




