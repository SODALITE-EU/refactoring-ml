import warnings
from math import sqrt

from sklearn.metrics import mean_squared_error
from statsmodels.tsa.arima.model import ARIMA
from statsmodels.tsa.arima_model import ARIMAResults


#
# # monkey patch around bug in ARIMA class
def __getnewargs__(self):
    return ((self.endog), (self.k_lags, self.k_diff, self.k_ma))


ARIMA.__getnewargs__ = __getnewargs__


# evaluate an ARIMA model for a given order (p,d,q)
def evaluate_arima_model(dataset, arima_order):
    train_size = int(len(dataset) * 0.66)
    train, test = dataset[0:train_size], dataset[train_size:]
    # prepare training dataset
    history = [x for x in train]
    # make predictions
    predictions = list()
    for t in range(len(test)):
        model = ARIMA(history, order=arima_order)
        model_fit = model.fit()
        yhat = model_fit.forecast()[0]
        predictions.append(yhat)
        history.append(test[t])
    # calculate out of sample error
    rmse = sqrt(mean_squared_error(test, predictions))
    return rmse


# evaluate combinations of p, d and q values for an ARIMA model
def evaluate_models(dataset, p_values, d_values, q_values):
    dataset = dataset.astype('float32')
    best_score, best_cfg = float("inf"), None
    for p in p_values:
        for d in d_values:
            for q in q_values:
                order = (p, d, q)
                try:
                    rmse = evaluate_arima_model(dataset, order)
                    if rmse < best_score:
                        best_score, best_cfg = rmse, order
                    print('ARIMA%s RMSE=%.3f' % (order, rmse))
                except:
                    continue
    return best_cfg, best_score


def save_model(dataset, m_name):
    model_fit, predicion = fit_forecast_next(dataset)
    model_fit.save(m_name + '.pkl', True)


def fit_forecast_next(dataset):
    p_values = [0, 1, 2, 4, 6, 8, 10]
    d_values = range(0, 3)
    q_values = range(0, 3)
    warnings.filterwarnings("ignore")
    best_cfg, best_score = evaluate_models(dataset, p_values, d_values, q_values)
    print('Best ARIMA%s RMSE=%.3f' % (best_cfg, best_score))
    model = ARIMA(dataset, order=best_cfg)
    model.k_lags = None
    model_fit = model.fit()
    return model_fit, model_fit.forecast()


def forecast_next(m_name):
    loaded = ARIMAResults.load(m_name + '.pkl')
    return loaded.predict()
