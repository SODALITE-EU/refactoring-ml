import unittest
from datetime import datetime

from pandas import read_csv

import timeseries.arima_grid as armia
from regression.linear import fit_forecast_next_multi
from regression.polynomial import fit_forecast
from timeseries import var_multi


class TestForcaster(unittest.TestCase):

    def test_armia(self):
        # load dataset
        def parser(x):
            return datetime.strptime('190' + x, '%Y-%m')

        series = read_csv('testResources/shampoo.csv', header=0, index_col=0, parse_dates=True, squeeze=True,
                          date_parser=parser)
        # evaluate parameters
        model_fit, prediction = armia.fit_forecast_next(series.values)
        assert model_fit is not None
        assert prediction is not None

    def test_multi(self):
        df = read_csv("testResources/dataset_multi_time.csv")
        ds = df.drop(['Time'], axis=1)
        ds2 = ds.copy()

        forecast, valid, rmses = var_multi.fit_forecast(ds)
        forecast2 = var_multi.fit_forecast_next(ds2)
        assert forecast is not None
        assert forecast2 is not None
        assert rmses is not None

    def test_ploy(self):
        datas = read_csv('testResources/polydata.csv')
        X = datas.iloc[:, 0:1].values
        y = datas.iloc[:, 1].values
        linear, poly = fit_forecast(datas)
        result2 = linear.predict(poly.fit_transform(X))
        datas2 = read_csv('testResources/multilinear.csv')
        result1 = fit_forecast_next_multi(datas2, [[1203, 3]])
        assert result1 is not None
        assert result2 is not None
