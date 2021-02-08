# Importing the libraries
import matplotlib.pyplot as plt
import pandas as pd

from regression.linear import fit_forecast_next_multi
# Importing the dataset
from regression.polynomial import fit_forecast

datas = pd.read_csv('../testResources/polydata.csv')
print(datas)

X = datas.iloc[:, 0:1].values
y = datas.iloc[:, 1].values

# Visualising the Polynomial Regression results
plt.scatter(X, y, color='blue')
linear, poly = fit_forecast(datas)
plt.plot(X, linear.predict(poly.fit_transform(X)), color='red')
plt.title('Polynomial Regression')
plt.xlabel('Temperature')
plt.ylabel('Pressure')

plt.show()

datas2 = pd.read_csv('../testResources/multilinear.csv')
print(datas2)
print(fit_forecast_next_multi(datas2, [[1203, 3]]))  # 1203,3,239500
