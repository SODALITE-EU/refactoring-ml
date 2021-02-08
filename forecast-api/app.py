import pandas as pd
from flask import Flask, json, request, Response

import timeseries.arima_grid as armia
from regression import linear

app = Flask(__name__)
app.config["DEBUG"] = True


@app.route('/forecast-api/<model>/forecast/<x>', methods=['POST'])
def forecast_next(model, x):
    test_x = float(x)
    content = request.get_json()
    df = pd.read_json(json.dumps(content), orient='records')
    series = pd.Series(df['Y'].values, index=df['X'])
    if model == "arima":
        model_fit, prediction = armia.fit_forecast_next(series.values)

    elif model == "linear":
        prediction = linear.fit_forecast_next(df, test_x)
    else:
        prediction = -1
    text_out = {
        "prediction": prediction
    }
    js = json.dumps(text_out, sort_keys=False, indent=4)
    resp = Response(js, status=200, mimetype='application/json')
    resp.headers['Access-Control-Allow-Origin'] = '*'
    resp.headers['Access-Control-Allow-Methods'] = 'POST'
    resp.headers['Access-Control-Max-Age'] = '1000'
    return resp


app.run(host='0.0.0.0', port=5000)
