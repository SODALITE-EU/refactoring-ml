import base64

import pandas as pd
from flask import Flask, json, request, Response
from sklearn.model_selection import train_test_split

from utils.data_scraping import scrape_data
from utils.model_trainer import train_dr, train_rf, train_adaboost

app = Flask(__name__)
app.config["DEBUG"] = True


@app.route('/made/scape', methods=['POST'])
def scape_data(model):
    req_data = request.get_json()
    ip = req_data['ip']
    port = req_data['port']
    job_list = req_data['jobs']
    if job_list is None:
        job_list = ['envoy-stats', 'istiod', 'kube-state-metrics', 'kubernetes-apiservers', 'kubernetes-cadvisor',
                    'kubernetes-nodes', 'kubernetes-pods', 'kubernetes-service-endpoints', 'litmuschaos', 'skydive']

    scrape_data(ip, port, job_list)
    return json.dumps({'message': 'job data was scraped'}, sort_keys=False, indent=4), 200


@app.route('/made/<model>/train', methods=['POST'])
def train_models(model):
    # Import data
    df = pd.read_pickle('feature selection/multi-class-final7.pkl')
    # Remove remaining features that cause leakage.
    df[['Anomaly_nr',
        'minimum_container_memory_usage_bytes#amd64#e2-custom-8-12288#linux#pd-standard#docker#default-pool#cos#e2#europe-west3#europe-west3-a#/#gke-made-default-pool-e1ed2951-hgtl#kubernetes-cadvisor#amd64#gke-made-default-pool-e1ed2951-hgtl#linux#e2-custom-8-12288#5#europe-west3-a#europe-west3#europe-west3-a']].to_csv(
        'feature selection/visual-thesis.csv')
    # Split data into stratified train/test sets

    x, x_test, y, y_test = train_test_split(df.drop(
        ['Anomaly_nr', 'Anomaly_name'] + [column for column in df.columns if 'scrape_samples' in column] + [column for
                                                                                                            column in
                                                                                                            df.columns
                                                                                                            if
                                                                                                            'prometheus_tsdb_head_series' in column] + [
            column for column in df.columns if 'prometheus_tsdb_out_of_order' in column] + [column for column in
                                                                                            df.columns if
                                                                                            'scrape_series_added' in column] + [
            column for column in df.columns if 'prometheus_sd_kubernetes_events' in column] + [column for column in
                                                                                               df.columns if
                                                                                               'goroutines' in column],
        axis=1), df['Anomaly_nr'], test_size=0.2, train_size=0.8, random_state=1, stratify=df['Anomaly_nr'])
    if model == "dt":
        js = train_dr(df, x_test, y_test)
    elif model == "rf":
        js = train_rf(df)
    else:
        js = train_adaboost(df)
    resp = Response(js, status=200, mimetype='application/json')
    resp.headers['Access-Control-Allow-Origin'] = '*'
    resp.headers['Access-Control-Allow-Methods'] = 'POST'
    resp.headers['Access-Control-Max-Age'] = '1000'
    return resp


@app.route('/made/<model>/predict', methods=['POST'])
def predict_perf(model):
    content = request.get_json()
    df = pd.read_json(json.dumps(content), orient='records')
    js = json.dumps({'message': 'prediction was called'}, sort_keys=False, indent=4)
    resp = Response(js, status=200, mimetype='application/json')
    resp.headers['Access-Control-Allow-Origin'] = '*'
    resp.headers['Access-Control-Allow-Methods'] = 'POST'
    resp.headers['Access-Control-Max-Age'] = '1000'
    return resp


@app.route('/per-predictor/<model>/explain-global', methods=['GET'])
def predict_perf(model):
    js = read_file("force_plot_" + model + ".svg")
    resp = Response(js, status=200, mimetype='application/json')
    return resp


def read_file(name):
    with open(name, "rb") as image_file:
        return {"image": base64.b64encode(image_file.read())}


app.run(host='0.0.0.0', port=5000)
