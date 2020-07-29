import json

import utils.DTR as dtr_model
from utils.db_util import create_variant_table, add_data_records, read_data_records, drop_variant_table

with open('testResources/table.json') as json_file:
    columnsdata = json.load(json_file)

columns = columnsdata["features"] + columnsdata["metrics"]
create_variant_table(columns)

with open('testResources/data.json') as json_file:
    data = json.load(json_file)
add_data_records(data)
df = read_data_records()
df = df.drop(columns=['id'])
dtr_model.train(df)
drop_variant_table()
value = dtr_model.predict(df.iloc[9:10, 0:14])
print(value)
