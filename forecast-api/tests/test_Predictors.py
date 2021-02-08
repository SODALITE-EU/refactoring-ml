from datetime import datetime

from pandas import read_csv

import timeseries.arima_grid as armia


class TestPredictors:

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
