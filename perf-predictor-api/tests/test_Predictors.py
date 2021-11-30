import pandas as pd
import mlalgo.DTR as dtr_model


class TestPredictors:

    def test_dtr(self):
        with open('testResources/data.json') as json_file:
            data = pd.read_json(json_file)
        q = data["response_time"].max()
        assert 36816 == int(q)
        dtr_model.train(data)
        value = dtr_model.predict(data.iloc[9:10, 0:14])
        assert value is not None
