import pandas as pd
from flask import Flask, json, request, Response

import utils.DTR as dtr_model
import utils.MLPNN as mlpnn_model
import utils.RTFR as rtfr_model
from utils.db_util import create_variant_table, drop_variant_table, read_data_records, add_data_records

app = Flask(__name__)
app.config["DEBUG"] = True


def get_table_name(model):
    if model == "mlpnn":
        table_name = "variant_table_mlpnn"
    else:
        table_name = "variant_table"
    return table_name


@app.route('/per-predictor/{model}/features', methods=['POST'])
def create_table(model):
    req_data = request.get_json()
    features = req_data['features']
    metrics = req_data['metrics']
    columns = features + metrics
    create_variant_table(get_table_name(model), columns)
    return json.dumps({'message': 'variant table is recorded'}, sort_keys=False, indent=4), 200


@app.route('/per-predictor/{model}/features', methods=['DELETE'])
def delete_table(model):
    drop_variant_table(get_table_name(model))
    return json.dumps({'message': 'variant data are dropped'}, sort_keys=False, indent=4), 200


@app.route('/per-predictor/{model}/traindata', methods=['PUT'])
def update_data(model):
    content = request.get_json()
    data = pd.read_json(content, orient='records')
    add_data_records(get_table_name(model), data)
    return json.dumps({'message': 'variant data are updated'}, sort_keys=False, indent=4), 200


@app.route('/per-predictor/{model}/train', methods=['POST'])
def train_models(model):
    df = read_data_records(get_table_name(model))
    df = df.drop(columns=['id'])
    if model == "rtfr":
        js = rtfr_model.train(df)
    elif model == "mlpnn":
        js = mlpnn_model.train(df)
    else:
        js = dtr_model.train(df)
    resp = Response(js, status=200, mimetype='application/json')
    resp.headers['Access-Control-Allow-Origin'] = '*'
    resp.headers['Access-Control-Allow-Methods'] = 'POST'
    resp.headers['Access-Control-Max-Age'] = '1000'


@app.route('/per-predictor/{model}/predict', methods=['POST'])
def predict_perf(model):
    content = request.get_json()
    df = pd.read_json(content, orient='records')
    if model == "rtfr":
        js = rtfr_model.predict(df)
    elif model == "mlpnn":
        js = mlpnn_model.predict(df)
    else:
        js = dtr_model.predict(df)
    resp = Response(js, status=200, mimetype='application/json')
    resp.headers['Access-Control-Allow-Origin'] = '*'
    resp.headers['Access-Control-Allow-Methods'] = 'POST'
    resp.headers['Access-Control-Max-Age'] = '1000'


app.run(host='0.0.0.0', port=5000)
