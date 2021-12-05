import shap

shap.initjs()


def local_plot(name, explainer, shap_values, feature_names, chosen_sample, estimand_name, X_test,
               plot_type='force_plot'):
    if plot_type == 'force_plot':
        h = shap.force_plot(
            base_value=explainer.expected_value,
            shap_values=shap_values[chosen_sample],
            features=X_test[chosen_sample],
            feature_names=feature_names,
            link="identity",
            out_names=estimand_name, matplotlib=False, show=False)
        save_plot(h, name)
    elif plot_type == 'decision_plot':
        h = shap.decision_plot(
            base_value=explainer.expected_value,
            shap_values=shap_values[chosen_sample],
            features=X_test[chosen_sample],
            feature_names=feature_names,
            link="identity", matplotlib=False, show=False
        )
        save_plot(h, name)
    return h


def save_plot(plt, name):
    shap.save_html("plots/" + name, plt)


def global_plot(name, explainer, shap_values, X_test, feature_names, plot_type='dot'):
    if plot_type == 'force_plot':
        h = shap.force_plot(
            base_value=explainer.expected_value,
            shap_values=shap_values,
            features=X_test,
            feature_names=feature_names,
            link="identity", matplotlib=False, show=False)
    else:
        h = shap.summary_plot(
            shap_values=shap_values,
            features=X_test,
            max_display=20,
            feature_names=feature_names,
            plot_type=plot_type, show=False)
    save_plot(h, name)
    return h
