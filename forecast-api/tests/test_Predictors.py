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
        print(series)
        model_fit, prediction = armia.fit_forcast_next(series.values)
        print(prediction)
