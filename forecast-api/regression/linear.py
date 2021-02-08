import numpy as np
from sklearn import linear_model


def fit_forecast_next(dataset, test_x):
    model = fit_forecast(dataset)
    return model.predict(np.array([test_x]))[0]


def fit_forecast(dataset):
    reg = linear_model.LinearRegression()
    y_loc = len(dataset.columns) - 1
    x = dataset.iloc[:, 0:y_loc].values
    print(x)
    y = dataset.iloc[:, y_loc].values
    model = reg.fit(x, y)
    return model


def fit_forecast_next_multi(dataset, test_x):
    model = fit_forecast(dataset)
    return model.predict(test_x)
