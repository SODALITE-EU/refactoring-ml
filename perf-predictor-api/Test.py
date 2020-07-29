import json

import utils.DTR as dtr_model
import utils.MLPNN as mlp_model
from utils.db_util import create_variant_table, add_data_records, read_data_records, drop_variant_table

with open('testResources/table.json') as json_file:
    columnsdata = json.load(json_file)

columns = columnsdata["features"] + columnsdata["metrics"]
create_variant_table("variant_table", columns)

with open('testResources/data.json') as json_file:
    data = json.load(json_file)
add_data_records("variant_table", data)
df = read_data_records("variant_table")
df = df.drop(columns=['id'])
dtr_model.train(df)
drop_variant_table("variant_table")
value = dtr_model.predict(df.iloc[9:10, 0:14])
print(value)

with open('testResources/table_nn.json') as json_file:
    columnsdata1 = json.load(json_file)

columns1 = columnsdata1["features"] + columnsdata1["metrics"]
create_variant_table("variant_table_mlpnn", columns1)

with open('testResources/data_nn.json') as json_file:
    data1 = json.load(json_file)
add_data_records("variant_table_mlpnn", data1)
df = read_data_records("variant_table_mlpnn")
df = df.drop(columns=['id'])
mlp_model.train(df)
drop_variant_table("variant_table_mlpnn")
value = mlp_model.predict(df.iloc[9:10, 0:14])
print(value)
