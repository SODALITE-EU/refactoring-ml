from utils.model_prediction import detect_anomalies


class TestPredictors:

    def test_prediction(self):
        value = detect_anomalies()
        assert value is not None
