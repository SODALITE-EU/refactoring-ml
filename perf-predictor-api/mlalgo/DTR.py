import json
import pickle

import pandas as pd
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import train_test_split
from sklearn.tree import DecisionTreeRegressor


def train_kfold_grid(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    # x = structured_data.iloc[:, 0:xindex]  # Feature Matrix
    y = structured_data['response_time']  # Target Variable
    return hyper_par_girdcv(x, y)


def train(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    y = structured_data['response_time']  # Target Variable

    best_params, dept_df, depth = param_tuningDTR(x, y)
    size_score = trainsize_gridsearch(x, y, best_params)
    size_score = size_score.iloc[size_score['r2_score'].idxmax()]
    best_train_size = round(1 - size_score[0], 2)

    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=best_train_size, random_state=42)
    mlregr_final = DecisionTreeRegressor(criterion=str(best_params[0]), splitter=best_params[1],
                                         max_depth=int(best_params[2]),
                                         max_features='auto')
    mlregr_final.fit(X_train, y_train)

    # Score
    model_score = mlregr_final.score(X_train, y_train)
    y_pred = mlregr_final.predict(X_test)

    text_out = {
        "score:": round(model_score, 2),
        "R-squared": round(r2_score(y_test, y_pred), 3),
        "MAE": round(mean_absolute_error(y_test, y_pred), 3),
        "MSE": round(mean_squared_error(y_test, y_pred), 3)
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)

    with open('models/dtr.pkl', 'wb') as output_file:
        pickle.dump(mlregr_final, output_file)

    return json_out


def hyper_par_girdcv(x, y):
    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=0.7, random_state=42)
    model = DecisionTreeRegressor()
    parameters = {"splitter": ["best", "random"],
                  "max_depth": range(1, 20),
                  "min_samples_leaf": [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
                  "max_features": ["auto", "log2", "sqrt", None],
                  "max_leaf_nodes": [None, 10, 20, 30, 40, 50, 60, 70, 80, 90]}
    gs = GridSearchCV(model,
                      param_grid={'max_depth': range(1, 20)},
                      cv=10,
                      n_jobs=1,
                      scoring='neg_mean_squared_error')

    gs.fit(X_train, y_train)
    print(gs.best_params_)
    best_grid = gs.best_estimator_
    y_pred = best_grid.predict(X_test)
    text_out = {
        "R-squared": round(r2_score(y_test, y_pred), 3),
        "MAE": round(mean_absolute_error(y_test, y_pred), 3),
        "MSE": round(mean_squared_error(y_test, y_pred), 3)
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)

    with open('models/dtr.pkl', 'wb') as output_file:
        pickle.dump(best_grid, output_file)

    return json_out


def param_tuningDTR(X, y):
    # returns the best params
    crit = []
    spli = []
    dep = []
    r2scr = []

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.7, random_state=42)

    dept_df = pd.DataFrame()
    dept_df['y'] = y_test.to_list()

    criterion = ['mse', 'friedman_mse', 'mae']
    for cr in criterion:
        split = ['random', 'best']
        for sp in split:
            depth = [2, 4, 6, 8, 10]
            for m in depth:
                mlregr = DecisionTreeRegressor(criterion=cr, splitter=sp, max_depth=m, max_features='auto')
                mlregr.fit(X_train, y_train)

                y_predicted = mlregr.predict(X_test)
                crit.append(cr)
                spli.append(sp)
                dep.append(m)
                r2_score_x = r2_score(y_test, y_predicted)
                r2scr.append(r2_score_x)
                dept_df["depth(" + str(m) + ")"] = y_predicted.tolist()

    history = pd.DataFrame(list(zip(crit, spli, dep, r2scr)),
                           columns=['criterion', 'splitter', 'max_depth', 'r2_score'])
    best_params = history.iloc[history['r2_score'].idxmax()]

    return best_params, dept_df, depth


def trainsize_gridsearch(X, y, best_params):
    scores = []
    sizes = [0.1, 0.2, 0.3, 0.4, 0.5]

    for size in sizes:
        XX_train, XX_test, yy_train, yy_test = train_test_split(X, y, test_size=size, random_state=42)

        mlregr_final = DecisionTreeRegressor(criterion=str(best_params[0]), splitter=best_params[1],
                                             max_depth=best_params[2],
                                             max_features='auto')
        mlregr_final.fit(XX_train, yy_train)

        # Score
        # model_score = mlregr_final.score(XX_train, yy_train)
        y_pred = mlregr_final.predict(XX_test)
        scores.append(round(r2_score(yy_test, y_pred), 2))
    size_score = pd.DataFrame(list(zip([1 - s for s in sizes], scores)), columns=['train_size', 'r2_score'])

    return size_score


def predict(variant):
    with open('models/dtr.pkl', 'rb') as in_file:
        clf2 = pickle.load(in_file)
    value = clf2.predict(variant)
    text_out = {
        "prediction": value[0]
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)
    return json_out
