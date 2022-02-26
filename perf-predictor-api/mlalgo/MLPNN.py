import json
import pickle

import pandas as pd
from keras.layers import Dense
from keras.models import Sequential
from keras.models import load_model
from keras.optimizers_v1 import Adam
from sklearn.metrics import r2_score, mean_squared_error, mean_absolute_error
from sklearn.model_selection import GridSearchCV
from sklearn.model_selection import train_test_split
from sklearn.neural_network import MLPRegressor


def train_kfold_grid(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    # x = structured_data.iloc[:, 0:xindex]  # Feature Matrix
    y = structured_data['response_time']  # Target Variable
    return hyper_par_girdcv(x, y)


def hyper_par_girdcv(x, y):
    X_train, X_test, y_train, y_test = train_test_split(x, y, test_size=0.7, random_state=42)
    parameters = {
        'hidden_layer_sizes': [(8, 8, 8, 8), (16, 16, 16, 16), (32, 32, 32, 32), (64, 64, 64, 64), (128, 128, 128, 128),
                               (256, 256, 256, 256)],
        # 'activation': ['tanh', 'relu'],
        'solver': ['adam'],
        #             'alpha':  [0.0001, 0.05],
        #             'learning_rate': ['constant','adaptive'],
    }

    mlp = MLPRegressor(activation='relu', alpha=0.001, learning_rate='adaptive', shuffle=False, verbose=0,
                       batch_size=100)
    gs = GridSearchCV(mlp,
                      param_grid=parameters,
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

    with open('models/mlpnn.pkl', 'wb') as output_file:
        pickle.dump(best_grid, output_file)

    return json_out


def train(structured_data):
    x = structured_data.loc[:, structured_data.columns != 'response_time']
    y = structured_data['response_time']
    # Perform grid search and get optimal params
    train_sizes = [0.1, 0.2, 0.3, 0.4, 0.5]
    # Perform grid search and get optimal params
    # train_sizes = [0.4,0.5]
    units, best_scores, neurons, sizes, all_units = deep_layer_neurons(x, y, train_sizes)
    best = best_scores.iloc[best_scores['r2_score'].idxmax()]
    best_train_size = round(1 - best[0], 2)
    best_neurons = int(best[2])
    # Optimal NN model
    XX_train, XX_test, yy_train, yy_test = train_test_split(x, y, test_size=best_train_size, random_state=42)

    model_final = Sequential()
    model_final.add(Dense(units, input_shape=(14,), kernel_initializer='normal', activation='relu'))
    model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
    model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
    model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
    model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
    model_final.add(Dense(1, kernel_initializer='normal', activation='linear'))
    model_final.compile(Adam(lr=0.001), 'mean_squared_error')

    model_final.fit(XX_train, yy_train, epochs=200, validation_split=0.2, shuffle=False, verbose=0, batch_size=100)
    y_pred = model_final.predict(XX_test)

    text_out = {
        "R-squared": round(r2_score(yy_test, y_pred)),
        "MAE": round(mean_absolute_error(yy_test, y_pred), 3),
        "MSE": round(mean_squared_error(yy_test, y_pred), 3)
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)

    model_final.save('models/mlpnn.h5')

    return json_out


def deep_layer_neurons(X, Y, sizes):
    # perfroms grid search to find best neurons for hidden layers
    scores = []
    neur = []

    for size in sizes:
        X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=size, random_state=42)

        neurons = pd.DataFrame()
        neurons['y_test'] = y_test.to_list()
        neuron_cnt = []
        r2_sco = []
        all_units = [8, 16, 32, 64, 128, 256]

        for un in all_units:
            model = Sequential()
            model.add(Dense(un, input_shape=(14,), kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(1, kernel_initializer='normal', activation='linear'))
            model.compile(Adam(lr=0.001), 'mean_squared_error')

            history = model.fit(X_train, y_train, epochs=200, validation_split=0.2, shuffle=False, verbose=0,
                                batch_size=100)

            y_test_pred = model.predict(X_test)
            neurons["(" + str(un) + ") neurons"] = y_test_pred.flatten().tolist()

            neuron_cnt.append(un)
            r2_sco.append(r2_score(y_test, y_test_pred))

        param_hist = pd.DataFrame(list(zip(neuron_cnt, r2_sco)), columns=['neurons', 'r2_sco'])
        best_params = param_hist.iloc[param_hist['r2_sco'].idxmax()]
        units = int(best_params[0])
        neur.append(units)
        scores.append(round(r2_score(y_test, y_test_pred), 2))

    best_scores = pd.DataFrame(list(zip([1 - s for s in sizes], scores, neur)),
                               columns=['train_size', 'r2_score', 'neurons'])
    # best = best_scores.iloc[best_scores['r2_score'].idxmax()]

    # Return the accuracies (best_scores) at every neuron size and number of best neurons(units) and train size
    return units, best_scores, neurons, sizes, all_units


def predict(variant):
    clf2 = load_model('models/mlpnn.h5')
    value = clf2.predict(variant)
    text_out = {
        "prediction": float(value[0][0])
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)
    return json_out
