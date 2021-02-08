# Importing libraries
import pandas as pd
from numpy import sqrt
from sklearn.metrics import mean_squared_error
from statsmodels.tsa.api import VAR
from statsmodels.tsa.stattools import adfuller


def adf_test(ds):
    dftest = adfuller(ds, autolag='AIC')
    adf = pd.Series(dftest[0:4], index=['Test Statistic', 'p-value', '# Lags', '# Observations'])

    for key, value in dftest[4].items():
        adf['Critical Value (%s)' % key] = value

    p = adf['p-value']
    if p <= 0.05:
        print("\nSeries is Stationary")
        return True
    else:
        print("\nSeries is Non-Stationary")
        return False


def remove_stationary(ds_train):
    stationary = False
    for i in ds_train.columns:
        stationary = adf_test(ds_train[i])
        if stationary:
            break
    if stationary:
        ds_differenced = ds_train.diff().dropna()
        stationary = False
        for i in ds_differenced.columns:
            stationary = adf_test(ds_train[i])
            if stationary:
                break
        if stationary:
            ds_differenced = ds_differenced.diff().dropna()
            return ds_differenced, 2
        else:
            return ds_differenced, 1
    else:
        return ds_train, 0


def fit_forecast_next(dataset):
    cols = dataset.columns
    # creating the train and validation set
    train = dataset[:int(0.8 * (len(dataset)))]
    valid = dataset[int(0.8 * (len(dataset))):]
    train_differenced, round_no = remove_stationary(train)
    model = VAR(train_differenced)
    model_fit = model.fit()
    # make prediction on validation
    prediction = model_fit.forecast(model_fit.endog, steps=len(valid))
    # converting predictions to dataframe
    forecast = pd.DataFrame(prediction, index=dataset.index[-len(valid):], columns=cols)
    if round_no != 0:
        forecast = invert_transformation(train, forecast, (round_no == 2))
    # check rmse
    rmses = {}
    for i in cols:
        rmses[i + '_RMSE'] = sqrt(mean_squared_error(forecast[i], valid[i]))

    return forecast, valid, rmses


# Inverting the Differencing Transformation
def invert_transformation(ds, df_forecast, second_diff=False):
    for col in ds.columns:
        # Undo the 2nd Differencing
        if second_diff:
            df_forecast[str(col)] = (ds[col].iloc[-1] - ds[col].iloc[-2]) + df_forecast[str(col)].cumsum()

        # Undo the 1st Differencing
        df_forecast[str(col)] = ds[col].iloc[-1] + df_forecast[str(col)].cumsum()

    return df_forecast
