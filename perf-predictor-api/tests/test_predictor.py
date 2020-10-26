import json
import pytest


class TestPredictor:

    def test_file(self):
        with open('testResources/table.json') as json_file:
            columnsdata = json.load(json_file)

        columns = columnsdata["features"] + columnsdata["metrics"]
        assert 15 == len(columns)

