import json
import pickle

from utils.db_util import create_variant_table, add_data_records, read_data_records, drop_variant_table
from utils.DTR import train
with open('testResources/table.json') as json_file:
    columnsdata = json.load(json_file)

columns = columnsdata["features"] + columnsdata["metrics"]
create_variant_table(columns)

with open('testResources/data.json') as json_file:
    data = json.load(json_file)
add_data_records(data)
df = read_data_records()
df = df.drop(columns=['id'])
train(df)
drop_variant_table()
with open('models/dtr.pkl', 'rb') as in_file:
    clf2 = pickle.load(in_file)
value = clf2.predict(df.iloc[9:10, 0:14])
print(value[0])