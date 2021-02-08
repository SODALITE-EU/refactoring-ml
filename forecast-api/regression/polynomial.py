from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import PolynomialFeatures


def fit_forecast_next(dataset, test_x):
    linear, poly = fit_forecast(dataset)
    return linear.predict(poly.fit_transform(test_x))


def fit_forecast(dataset):
    y_loc = len(dataset.columns) - 1
    print(y_loc)
    X = dataset.iloc[:, 0:y_loc].values
    y = dataset.iloc[:, y_loc].values
    poly = PolynomialFeatures(degree=4)
    X_poly = poly.fit_transform(X)
    poly.fit(X_poly, y)
    linear = LinearRegression()
    linear.fit(X_poly, y)
    return linear, poly
