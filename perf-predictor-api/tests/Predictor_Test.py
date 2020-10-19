import json
import unittest


class TestPredictor(unittest.TestCase):

    def test_file(self):
        with open('testResources/table.json') as json_file:
            columnsdata = json.load(json_file)

        columns = columnsdata["features"] + columnsdata["metrics"]
        self.assertEqual(15, len(columns))


if __name__ == '__main__':
    unittest.main()
