from sqlalchemy import create_engine, Column, Float, Table, MetaData, String, Integer
import sqlalchemy as db
import pandas as pd

engine = db.create_engine('sqlite:///data/traindata.db', echo=True)
metadata = MetaData()


def create_variant_table(column_names):
    conn = engine.connect()
    trans = conn.begin()
    columns = (Column(name, Float, quote=False) for name in column_names)
    v_table = Table('variant_table', metadata, Column('id', Integer, primary_key=True, autoincrement=True), *columns)
    v_table.create(engine, checkfirst=True)
    trans.commit()


def drop_variant_table():
    conn = engine.connect()
    trans = conn.begin()
    v_table = metadata.tables['variant_table']
    v_table.drop(engine, checkfirst=True)
    trans.commit()


def add_data_records(records):
    v_table = metadata.tables['variant_table']
    query = db.insert(v_table)
    connection = engine.connect()
    trans = connection.begin()
    connection.execute(query, records)
    trans.commit()


def read_data_records():
    v_table = metadata.tables['variant_table']
    connection = engine.connect()
    trans = connection.begin()
    query = db.select([v_table])
    df = pd.read_sql_query(query, con = connection)
    # results = connection.execute().fetchall()
    # df = pd.DataFrame(results)
    trans.commit()
    return df