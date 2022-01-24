# Import packages
import json
import pickle

import numpy as np

np.random.seed(1)


def detect_anomalies(model_file='models/decision_tree/models/best_decision_tree.pkl'):
    with open(model_file, 'rb') as in_file:
        clf2 = pickle.load(in_file)
    # value = clf2.predict(variant)
    text_out = {
        "prediction": None
    }
    json_out = json.dumps(text_out, sort_keys=False, indent=4)
    return json_out
