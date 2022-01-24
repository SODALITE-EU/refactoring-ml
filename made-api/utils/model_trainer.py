# Import packages
import pickle

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

np.random.seed(1)
from sklearn.model_selection import train_test_split, RandomizedSearchCV
from sklearn.ensemble import RandomForestClassifier, AdaBoostClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.metrics import confusion_matrix
import sklearn.metrics as metrics


# This functions runs Random Forest models using different parameters from the random_grid.
# Results are saved in a dataframe and returned.
def runDecisionTree(df, max_features, max_depth, min_samples_split, min_samples_leaf, n_iter, test_size, train_size):
    # Different parameters are initialized.
    random_grid = {'max_features': max_features,
                   'max_depth': max_depth,
                   'min_samples_split': min_samples_split,
                   'min_samples_leaf': min_samples_leaf}

    # Random Forest Classifier model is initialized.
    DTClassifierModel = DecisionTreeClassifier()

    # Randomized Search is being used with different parameters (from random_grid).
    dt_random = RandomizedSearchCV(estimator=DTClassifierModel, param_distributions=random_grid,
                                   scoring=['accuracy', 'precision_macro', 'recall_macro', 'f1_macro', 'f1_weighted'],
                                   n_iter=n_iter, cv=10, verbose=2, random_state=1, n_jobs=-1, refit='accuracy',
                                   return_train_score=True)

    # Split data into stratified train/test sets

    x, x_test, y, y_test = train_test_split(df.drop(
        ['Anomaly_nr', 'Anomaly_name'] + [column for column in df.columns if 'scrape_samples' in column] + [column for
                                                                                                            column in
                                                                                                            df.columns
                                                                                                            if
                                                                                                            'prometheus_tsdb_head_series' in column] + [
            column for column in df.columns if 'prometheus_tsdb_out_of_order' in column], axis=1), df['Anomaly_nr'],
        test_size=0.2, train_size=0.8, random_state=1, stratify=df['Anomaly_nr'])
    # Model is fitted with the x and y data.
    dt_random.fit(x, y)

    # Results are assigned to a new dataframe and shown.
    dfResult = pd.DataFrame(dt_random.cv_results_)

    return dfResult, dt_random


def train_dr(df, x_test, y_test):
    # Running decision tree models with different parameters.
    # Results are stored in a DataFrame.

    max_features = np.linspace(0, 1, 11, endpoint=True)[1:]
    max_depth = range(10, 101)
    min_samples_split = range(2, 31)
    min_samples_leaf = range(1, 21)

    dfResults, dt_model = runDecisionTree(df, max_features, max_depth, min_samples_split, min_samples_leaf, 1, 0.2, 0.8)

    # Rank results
    dfResults['Ranking'] = dfResults['mean_test_accuracy'].rank(method='max', ascending=False)

    dfResults = dfResults[['Ranking', 'mean_train_accuracy', 'mean_test_accuracy', 'mean_test_f1_macro', 'f1_weighted',
                           'mean_test_precision_macro', 'mean_test_recall_macro', 'param_min_samples_split',
                           'param_min_samples_leaf', 'param_max_features', 'param_max_depth', 'mean_fit_time',
                           'mean_score_time']].sort_values(by='mean_test_accuracy', ascending=False)
    dfResults.set_index('Ranking', inplace=True)

    # Save model and results in pickle file
    with open('models/decision_tree/models/best_decision_tree.pkl', 'wb') as fid:
        pickle.dump(dt_model.best_estimator_, fid)

    dfResults.to_pickle('models/decision_tree/results/results_final.pkl')
    # Load results
    dfResults = pd.read_pickle('models/decision_tree/results/results_final.pkl')
    # Load best decision tree
    with open('models/decision_tree/models/best_decision_tree.pkl', 'rb') as fid:
        dt_model = pickle.load(fid)

    return metrics.classification_report(dt_model.predict(x_test), y_test)


# This functions runs Random Forest models using different parameters from the random_grid.
# Results are saved in a dataframe and returned.
def runRandomForest(df, n_estimators, max_features, max_depth, min_samples_split, min_samples_leaf, n_iter, test_size,
                    train_size):
    # Different parameters are initialized.
    random_grid = {'n_estimators': n_estimators,
                   'max_features': max_features,
                   'max_depth': max_depth,
                   'min_samples_split': min_samples_split,
                   'min_samples_leaf': min_samples_leaf}

    # Random Forest Classifier model is initialized.
    RFClassifierModel = RandomForestClassifier()

    # Randomized Search is being used with different parameters (from random_grid).
    rf_random = RandomizedSearchCV(estimator=RFClassifierModel, param_distributions=random_grid,
                                   scoring=['accuracy', 'precision_macro', 'recall_macro', 'f1_macro', 'f1_weighted'],
                                   n_iter=n_iter, cv=10, verbose=2, random_state=1, n_jobs=-1, refit='accuracy',
                                   return_train_score=True)

    # Split data into stratified train/test sets

    x, x_test, y, y_test = train_test_split(df.drop(
        ['Anomaly_nr', 'Anomaly_name'] + [column for column in df.columns if 'scrape_samples' in column] + [column for
                                                                                                            column in
                                                                                                            df.columns
                                                                                                            if
                                                                                                            'prometheus_tsdb_head_series' in column] + [
            column for column in df.columns if 'prometheus_tsdb_out_of_order' in column], axis=1), df['Anomaly_nr'],
        test_size=0.2, train_size=0.8, random_state=1, stratify=df['Anomaly_nr'])
    # Model is fitted with the x and y data.
    rf_random.fit(x, y)

    # Results are assigned to a new dataframe and shown.
    dfResult = pd.DataFrame(rf_random.cv_results_)

    return dfResult, rf_random


def train_rf(df):
    # Running Random Forest models with different parameters.
    # Results are stored in a DataFrame.

    n_estimators = range(10, 101, 10)
    max_features = np.linspace(0, 1, 11, endpoint=True)[1:]
    max_depth = range(10, 101)
    min_samples_split = range(2, 21)
    min_samples_leaf = range(1, 11)

    dfResults, rf_model = runRandomForest(df, n_estimators, max_features, max_depth, min_samples_split,
                                          min_samples_leaf, 20, 0.2, 0.8)
    # Ranking models
    dfResults['Ranking'] = dfResults['mean_test_accuracy'].rank(method='max', ascending=False)

    dfResults = dfResults[['Ranking', 'mean_train_accuracy', 'mean_test_accuracy', 'mean_test_f1_macro', 'f1_weighted',
                           'mean_test_precision_macro', 'mean_test_recall_macro', 'param_n_estimators',
                           'param_min_samples_split', 'param_min_samples_leaf', 'param_max_features', 'param_max_depth',
                           'mean_fit_time', 'mean_score_time']].sort_values(by='mean_test_accuracy', ascending=False)
    dfResults.set_index('Ranking', inplace=True)

    # Save model and results in pickle file
    with open('models/random_forest/models/best_random_forest.pkl', 'wb') as fid:
        pickle.dump(rf_model.best_estimator_, fid)

    dfResults.to_pickle('models/random_forest/results/results_final.pkl')


# This functions runs Random Forest models using different parameters from the random_grid.
# Results are saved in a dataframe and returned.
def runAdaBoost(df, base_estimator, nr_estimators, learning_rate, n_iter, test_size, train_size):
    # Different parameters are initialized.
    random_grid = {'n_estimators': nr_estimators,
                   'learning_rate': learning_rate}

    # Random Forest Classifier model is initialized.
    ada_model = AdaBoostClassifier(base_estimator)

    # Randomized Search is being used with different parameters (from random_grid).
    dt_random = RandomizedSearchCV(estimator=ada_model, param_distributions=random_grid,
                                   scoring=['accuracy', 'precision_macro', 'recall_macro', 'f1_macro', 'f1_weighted'],
                                   n_iter=n_iter, cv=10, verbose=2, random_state=1, n_jobs=-1, refit='accuracy',
                                   return_train_score=True)

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
    # Model is fitted with the x and y data.
    dt_random.fit(x, y)

    # Results are assigned to a new dataframe and shown.
    dfResult = pd.DataFrame(dt_random.cv_results_)

    return dfResult, dt_random


def train_adaboost(df):
    # Running Adaboost models with different parameters.
    # Results are stored in a DataFrame.
    nr_estimators = range(40, 111, 10)
    learning_rate = np.linspace(0.2, 1, 11, endpoint=True)

    dt_model = DecisionTreeClassifier(min_samples_split=4, min_samples_leaf=17, max_features=0.2, max_depth=10)

    dfResults, ada_model = runAdaBoost(df, dt_model, nr_estimators, learning_rate, 1, 0.2, 0.8)
    # Rank models
    dfResults['Ranking'] = dfResults['mean_test_accuracy'].rank(method='max', ascending=False)

    dfResults = dfResults[['Ranking', 'mean_train_accuracy', 'mean_test_accuracy', 'mean_test_f1_macro', 'f1_weighted',
                           'mean_test_precision_macro', 'mean_test_recall_macro', 'param_learning_rate',
                           'param_n_estimators', 'mean_fit_time', 'mean_score_time']].sort_values(
        by='mean_test_accuracy', ascending=False)
    dfResults.set_index('Ranking', inplace=True)

    # Save model and results in pickle file
    with open('models/adaboost/models/best_dt_adaboost.pkl', 'wb') as fid:
        pickle.dump(ada_model.best_estimator_, fid)

    dfResults.to_pickle('models/adaboost/results/results_final_ada_dt.pkl')
    with open('models/adaboost/models/best_dt_adaboost.pkl', 'rb') as fid:
        ada_model = pickle.load(fid)


# Function to get all evaluation metrics.
def get_evaluation_metrics(model, x_test, y_test, model_name):
    model_results = []
    model_results.append(model_name)
    y_pred = model.predict(x_test)
    model_results.append(metrics.accuracy_score(y_test, y_pred))
    model_results.append(metrics.recall_score(y_test, y_pred, average='macro'))
    model_results.append(metrics.precision_score(y_test, y_pred, average='macro'))
    model_results.append(metrics.fbeta_score(y_test, y_pred, average='macro', beta=0.5))
    model_results.append(metrics.f1_score(y_test, y_pred, average='weighted'))
    model_results = [model_results]
    return pd.DataFrame(model_results,
                        columns=['Model', 'Accuracy', 'Recall', 'Precision', 'F-Score', 'Weighted F-Score'])


# Save confusion matrix for each class.
def save_confusion_matrix(file_name, model, x_test, y_test):
    sns.set(rc={'figure.figsize': (16.7, 8.27)})

    fig, ax = plt.subplots(figsize=(16, 9))

    sns_plot = sns.heatmap(confusion_matrix(y_test, model.predict(x_test)), annot=True, cmap='Blues', fmt='g',
                           xticklabels=['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog',
                                        'Pod IO stress', 'Pod memory hog', 'Non-anomalous'],
                           yticklabels=['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog',
                                        'Pod IO stress', 'Pod memory hog', 'Non-anomalous'], linewidth=0.5)

    plt.xlabel('Predicted label', size=15)
    plt.ylabel('True label', size=15)

    sns_plot.figure.savefig('visuals/' + file_name, dpi=200)
    plt.show()


# Save confusion matrix anomalous vs non-anomalous.
def save_general_confusion_matrix(file_name, model, x_test, y_test):
    anomaly_check = []
    predictions = model.predict(x_test)
    anomaly_predicted = []
    for i in range(0, len(x_test)):
        if y_test[i] == 6:
            anomaly_check.append(0)
        else:
            anomaly_check.append(1)

        if predictions[i] == 6:
            anomaly_predicted.append(0)
        else:
            anomaly_predicted.append(1)

    fig, ax = plt.subplots(figsize=(16, 9))
    sns_plot = sns.heatmap(metrics.confusion_matrix(anomaly_check, anomaly_predicted), annot=True, cmap='Blues',
                           fmt='g', xticklabels=['Non-Anomalous', 'Anomalous'],
                           yticklabels=['Non-Anomalous', 'Anomalous'])
    plt.xlabel('Predicted label', size=15)
    plt.ylabel('True label', size=15)

    sns_plot.figure.savefig('visuals/' + file_name, dpi=200)
    plt.show()


# Get accuracies for each model.
def get_accuracies(x_test, y_test):
    models = ['Decision Tree', 'Random Forest', 'AdaBoost']
    file_results = ['models/decision_tree/results/results_final.pkl', 'models/random_forest/results/results_final.pkl',
                    'models/adaboost/results/results_final_ada_dt.pkl']
    model_files = ['models/decision_tree/models/best_decision_tree.pkl',
                   'models/random_forest/models/best_random_forest.pkl', 'models/adaboost/models/best_dt_adaboost.pkl']
    df = pd.DataFrame(columns=['Model', 'Train', 'Test', 'Validation'])
    for i in range(0, len(file_results)):
        data = pd.read_pickle(file_results[i])
        with open(model_files[i], 'rb') as fid:
            model = pickle.load(fid)

        y_pred = model.predict(x_test)

        df = df.append({'Model': models[i], 'Train': data.iloc[0]['mean_train_accuracy'],
                        'Test': data.iloc[0]['mean_test_accuracy'],
                        'Validation': metrics.accuracy_score(y_test, y_pred)}, ignore_index=True)

    return df


def gen_accuracy_plots(x_test, y_test):
    # Get accuracies for each model for train, test, and train set.
    df_accuracies = get_accuracies(x_test, y_test)
    df_accuracies.melt(id_vars=['Model'], var_name='Metric', value_name='Accuracy')
    # Generate bar plot for the accuracies.

    df_accuracies = get_accuracies(x_test, y_test)

    sns.set_context("notebook", font_scale=1.3, rc={"lines.linewidth": 1.5})

    g = sns.catplot(
        data=df_accuracies.melt(id_vars=['Model'], var_name='Metric', value_name='Accuracy'), kind="bar",
        x="Model", y="Accuracy", hue="Metric",
        ci="sd", palette="mako", alpha=.8, height=8, aspect=1
    )
    g.despine(left=True)
    g.set_axis_labels("Model", "Accuracy", size=20)
    g.set(ylim=(0, 1))

    g.savefig('visuals/ModelAccuracies.png', dpi=200)


def gen_confusion_matrices_plots(x_test, y_test):
    # Generate confusion matrices.

    model_names = ['AdaBoost', 'Decision Tree', 'Random Forest']
    model_paths = ['models/adaboost/models/best_dt_adaboost.pkl', 'models/decision_tree/models/best_decision_tree.pkl',
                   'models/random_forest/models/best_random_forest.pkl']
    df_metrics = pd.DataFrame(columns=['Model', 'Accuracy', 'Recall', 'Precision', 'F-Score', 'Weighted F-Score'])
    for i in range(0, len(model_names)):
        with open(model_paths[i], 'rb') as fid:
            model = pickle.load(fid)
        df_metrics = pd.concat([df_metrics, get_evaluation_metrics(model, x_test, y_test, model_names[i])])

        save_general_confusion_matrix('GeneralConfusionMatrix' + model_names[i], model, x_test, y_test)
        save_confusion_matrix('ConfusionMatrix' + model_names[i], model, x_test, y_test)


def gen_all_metrics(df_metrics):
    # Melt the accuracies for visualization purposes.
    df_metrics.melt(id_vars=['Model'], var_name='Metric', value_name='Value')
    # Save melted df
    df_metrics.melt(id_vars=['Model'], var_name='Metric', value_name='Value').to_pickle('Model_results.pkl')
    # Bar plot for all metrics for each model
    sns.set_context("notebook", font_scale=1.3, rc={"lines.linewidth": 1.5})

    g = sns.catplot(
        data=df_metrics.melt(id_vars=['Model'], var_name='Metric', value_name='Value'), kind="bar",
        x="Metric", y="Value", hue="Model",
        ci="sd", palette="mako", alpha=.8, height=8, aspect=1
    )
    g.despine(left=True)
    g.set_axis_labels("Metric", "Value", size=20)
    # g.set_xticklabels(size=12)
    # g.set_yticklabels(size=12)
    g.set(ylim=(0, 1))

    g.savefig('visuals/ModelPerformance.png', dpi=200)
