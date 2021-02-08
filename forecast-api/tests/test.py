import warnings
from datetime import datetime

import pandas as pd
from pandas import read_json, to_numeric

import timeseries.arima_grid as armia
# load dataset
def parser(x):
    return datetime.strptime('190' + x, '%Y-%m')


# series = read_csv('../testResources/shampoo.csv', header=0, index_col=0, parse_dates=True, squeeze=True,
#                   date_parser=parser)

df = read_json('../testResources/shampoo.json', orient='records')
print(df)
series = pd.Series(df['Sales'].values, index=df['Month'])
to_numeric(series)
# series.index = series.index.to_period('M')
print(series)
# evaluate parameters
# print(series)
warnings.filterwarnings("ignore")
model_fit, prediction = armia.fit_forecast_next(series.values)
print(prediction)
