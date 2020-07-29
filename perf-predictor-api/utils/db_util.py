import pandas as pd
import sqlalchemy as db
from sqlalchemy import Column, Float, Table, MetaData, Integer

engine = db.create_engine('sqlite:///data/traindata.db', echo=True)
metadata = MetaData()


def create_variant_table(table_name, column_names):
    conn = engine.connect()
    trans = conn.begin()
    columns = (Column(name, Float, quote=False) for name in column_names)
    v_table = Table(table_name, metadata, Column('id', Integer, primary_key=True, autoincrement=True), *columns)
    v_table.create(engine, checkfirst=True)
    trans.commit()


def drop_variant_table(table_name):
    conn = engine.connect()
    trans = conn.begin()
    v_table = metadata.tables[table_name]
    v_table.drop(engine, checkfirst=True)
    trans.commit()


def add_data_records(table_name, records):
    v_table = metadata.tables[table_name]
    query = db.insert(v_table)
    connection = engine.connect()
    trans = connection.begin()
    connection.execute(query, records)
    trans.commit()


def read_data_records(table_name):
    v_table = metadata.tables[table_name]
    connection = engine.connect()
    trans = connection.begin()
    query = db.select([v_table])
    df = pd.read_sql_query(query, con=connection)
    trans.commit()
    return df
