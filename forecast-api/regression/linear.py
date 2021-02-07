import numpy as np
from sklearn import linear_model


def fit_forcast_next(dataset):
    regr = linear_model.LinearRegression()
    x = dataset['X']
    y = dataset['Y']
    model = regr.fit(x, y)
    return model.predict(np.array([len(x) + 1]))
