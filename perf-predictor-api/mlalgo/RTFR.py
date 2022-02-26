import json
import pickle

import pandas as pd
import shap
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import train_test_split

from utils.xai_util import global_plot


def train_kfold_grid(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    # x = structured_data.iloc[:, 0:xindex]  # Feature Matrix
    y = structured_data['response_time']  # Target Variable
    return hyper_par_girdcv(x, y)


def hyper_par_girdcv(x, y):
    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=0.7, random_state=42)
    model = RandomForestRegressor()
    parameters = {"n_estimators": [100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000],
                  "min_samples_leaf": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                  "max_features": ["auto", "log2", "sqrt", None],
                  "max_leaf_nodes": [None, 10, 20, 30, 40, 50, 60, 70, 80, 90]}
    gs = GridSearchCV(model,
                      param_grid={'n_estimators': [100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000]},
                      cv=10,
                      n_jobs=1,
                      scoring='neg_mean_squared_error')

    gs.fit(X_train, y_train)
    best_grid = gs.best_estimator_
    y_pred = best_grid.predict(X_test)
    text_out = {
        "R-squared": round(r2_score(y_test, y_pred), 3),
        "MAE": round(mean_absolute_error(y_test, y_pred), 3),
        "MSE": round(mean_squared_error(y_test, y_pred), 3)
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)

    with open('models/rtfr.pkl', 'wb') as output_file:
        pickle.dump(best_grid, output_file)

    return json_out


def train(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    y = structured_data['response_time']  # Target Variable
    trees = [100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000]
    sizes = [0.1, 0.2, 0.3, 0.4, 0.5]
    size_score = perform_RFR_GridSearch(x, y, trees, sizes)

    best = size_score.iloc[size_score['r2_score'].idxmax()]
    bes_n_est = int(best[2])
    best_mx_feat = int(best[3])
    best_train_size = round(1 - best[0], 2)

    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=best_train_size, random_state=42)
    rfReg = RandomForestRegressor(n_estimators=bes_n_est, max_features=best_mx_feat)
    rfReg.fit(X_train, y_train)
    model_score = rfReg.score(X_train, y_train)
    y_pred = rfReg.predict(X_test)

    text_out = {
        "score:": model_score,
        "R-squared": round(r2_score(y_test, y_pred), 3),
        "MAE": round(mean_absolute_error(y_test, y_pred), 3),
        "MSE": round(mean_squared_error(y_test, y_pred), 3)
    }

    json_out = json.dumps(text_out, sort_keys=False, indent=4)

    with open('models/rtfr.pkl', 'wb') as output_file:
        pickle.dump(rfReg, output_file)

    explainer = shap.TreeExplainer(model=rfReg, model_output='raw')
    shap_values = explainer.shap_values(X_test)
    global_plot(name='force_plot_rtfr.html', explainer=explainer, shap_values=shap_values, X_test=X_test,
                feature_names=x.columns, plot_type='force_plot')
    return json_out


def perform_RFR_GridSearch(X, Y, trees, sizes):
    scores = []
    best_tree = []
    best_feat = []

    for size in sizes:
        param_hist = pd.DataFrame()
        mx_feat = []
        n_trees = []
        r2_sc = []
        X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=size, random_state=42)

        for x in trees:
            for d in range(1, 15):
                rnd_forest_Reg = RandomForestRegressor(n_estimators=x, max_features=d)
                rnd_forest_Reg.fit(X_train, y_train)
                y_pred = rnd_forest_Reg.predict(X_test)
                r2 = r2_score(y_test, y_pred)
                n_trees.append(x)
                mx_feat.append(d)
                r2_sc.append(r2)

        param_hist = pd.DataFrame(list(zip(n_trees, mx_feat, r2_sc)),
                                  columns=['n_estimators', 'max_features', 'r2_score'])
        best_params = param_hist.iloc[param_hist['r2_score'].idxmax()]

        best_tree.append(int(best_params[0]))
        best_feat.append(int(best_params[1]))
        scores.append(round(best_params[2], 2))
    size_score = pd.DataFrame(list(zip([1 - s for s in sizes], scores, best_tree, best_feat)),
                              columns=['train_size', 'r2_score', 'no_trees', 'max_features'])

    return size_score


def predict(variant):
    with open('models/rtfr.pkl', 'rb') as in_file:
        rfReg2 = pickle.load(in_file)
    value = rfReg2.predict(variant)
    print(value)
    text_out = {
        "prediction": value[0]
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)
    return json_out
