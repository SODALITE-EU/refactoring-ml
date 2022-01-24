# Import packages
import pickle

import matplotlib.pyplot as plt
import numpy as np

np.random.seed(1)
import shap
from lime import lime_tabular


def explain_shap(x_test, model_file='models/decision_tree/models/best_decision_tree.pkl'):
    # Load best decision tree

    with open(model_file, 'rb') as fid:
        dt_model = pickle.load(fid)

    figure = plt.figure()

    # Show most important features and their importance to each class/anomaly
    shap.initjs()

    plt.figure(figsize=(14, 8), tight_layout=True)
    explainer = shap.TreeExplainer(dt_model)
    shap_values = explainer.shap_values(x_test)

    df_visual = x_test

    shap.summary_plot(shap_values, df_visual, plot_type='bar',
                      class_names=['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog', 'Pod IO stress',
                                   'Pod memory hog', 'Non-anomalous'], plot_size=(10, 8), max_display=10)

    figure.savefig('visuals/DT_Global_Shap_Fullname.png', dpi=200)

    plt.show()

    # Top 10 features with short names
    df_visual.columns = [column.split('#')[0] for column in x_test.columns]
    shap.summary_plot(shap_values, df_visual, plot_type='bar',
                      class_names=['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog', 'Pod IO stress',
                                   'Pod memory hog', 'Non-anomalous'], plot_size=(10, 8), max_display=10)
    figure.savefig('visuals/DT_Global_Shap_Shortname.png', dpi=200)

    # Global for 1 class.

    shap.summary_plot(shap_values[6], df_visual, feature_names=df_visual.columns)

    # Generate shap values on test set
    explainer = shap.TreeExplainer(dt_model)
    shap_values = explainer.shap_values(x_test)

    # Local interpretability

    shap.force_plot(explainer.expected_value[0], shap_values[0][0], x_test.iloc[0], feature_names=x_test.columns)

    # Local interpretability for class 6 on instance 4.
    class_number = 6
    row = 4

    shap.waterfall_plot(shap.Explanation(values=shap_values[class_number][row], base_values=explainer.expected_value[0],
                                         data=x_test.iloc[row],
                                         feature_names=x_test.columns.tolist()))

    # Local interpretability for class 0 on instance 11.
    with open('models/decision_tree/models/best_decision_tree.pkl', 'rb') as fid:
        dt_model = pickle.load(fid)

    explainer = shap.TreeExplainer(dt_model)
    shap_values = explainer.shap_values(x_test)

    class_number = 0
    row = 11

    shap.waterfall_plot(shap.Explanation(values=shap_values[class_number][row], base_values=explainer.expected_value[0],
                                         data=x_test.iloc[row],
                                         feature_names=x_test.columns.tolist()))
    # Short name version.
    class_number = 0
    row = 11

    shap.waterfall_plot(shap.Explanation(values=shap_values[class_number][row], base_values=explainer.expected_value[0],
                                         data=x_test.iloc[row],
                                         feature_names=x_test.columns.tolist()))


def explain_lime(df, x_test, y_test, model_file='models/adaboost/models/best_dt_adaboost.pkl'):
    # Load best adaboost
    with open(model_file, 'rb') as fid:
        ada_model = pickle.load(fid)

    filter_list = ['Anomaly_nr', 'Anomaly_name'] + [column for column in df.columns if 'scrape_samples' in column] + [
        column for column in df.columns if 'prometheus_tsdb_head_series' in column] + [column for column in df.columns
                                                                                       if
                                                                                       'prometheus_tsdb_out_of_order' in column] + [
                      column for column in df.columns if 'scrape_series_added' in column] + [column for column in
                                                                                             df.columns if
                                                                                             'prometheus_sd_kubernetes_events' in column] + [
                      column for column in df.columns if 'goroutines' in column]

    explainer = lime_tabular.LimeTabularExplainer(np.array(x_test), mode="classification",
                                                  class_names=list(set(y_test)),
                                                  feature_names=[column for column in x_test.columns if
                                                                 column not in filter_list], discretize_continuous=True)

    explanation = explainer.explain_instance(np.array(x_test)[1], ada_model.predict_proba, top_labels=10)
    explanation.show_in_notebook()

    explainer = lime_tabular.LimeTabularExplainer(np.array(x_test), mode="classification",
                                                  class_names=['Node CPU hog', 'Node IO stress', 'Node memory hog',
                                                               'Pod CPU hog', 'Pod IO stress', 'Pod memory hog',
                                                               'Non-anomalous'],
                                                  feature_names=[column for column in x_test.columns if
                                                                 column not in filter_list], discretize_continuous=True)
    explanation = explainer.explain_instance(np.array(x_test)[1], ada_model.predict_proba, top_labels=10)
    explanation.show_in_notebook()
