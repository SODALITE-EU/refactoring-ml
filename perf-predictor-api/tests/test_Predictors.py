import unittest

import pandas as pd

import mlalgo.DTR as dtr_model
import mlalgo.MLPNN as mlp_model
import mlalgo.RTFR as rt_model


class TestPredictors(unittest.TestCase):

    def test_dtr(self):
        with open('testResources/data.json') as json_file:
            data = pd.read_json(json_file)
        q = data["response_time"].max()
        assert 36816 == int(q)
        dtr_model.train(data)
        value = dtr_model.predict(data.iloc[9:10, 0:14])
        assert value is not None

    def test_dtr_kfold(self):
        with open('testResources/data.json') as json_file:
            data = pd.read_json(json_file)
        train_out = dtr_model.train_kfold_grid(data)
        assert train_out is not None
        value = dtr_model.predict(data.iloc[9:10, 0:14])
        assert value is not None

    def test_rf(self):
        with open('testResources/data.json') as json_file:
            data = pd.read_json(json_file)
        train_out = rt_model.train(data)
        assert train_out is not None
        value = rt_model.predict(data.iloc[9:10, 0:14])
        assert value is not None

    def test_rf_kfold(self):
        with open('testResources/data.json') as json_file:
            data = pd.read_json(json_file)
        train_out = rt_model.train_kfold_grid(data)
        assert train_out is not None
        value = rt_model.predict(data.iloc[9:10, 0:14])
        assert value is not None

    def test_mlp(self):
        with open('testResources/data_nn.json') as json_file:
            data = pd.read_json(json_file)
        train_out = mlp_model.train(data)
        assert train_out is not None
        value = mlp_model.predict(data.iloc[9:10, 0:14])
        assert value is not None

    def test_mlpkfold(self):
        with open('testResources/data_nn.json') as json_file:
            data = pd.read_json(json_file)
        train_out = mlp_model.train_kfold_grid(data)
        assert train_out is not None
        value = mlp_model.predict(data.iloc[9:10, 0:14])
        assert value is not None
