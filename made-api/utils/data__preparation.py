# Packages are imported.
import multiprocessing as mp
import os
import sys
import time

import numpy as np
import pandas as pd

np.random.seed(1)
sys.setrecursionlimit(25000)


# Litmus related features are removed.
def filter_chaos(dataset):
    filter_keywords_list = ['litmus', 'litmuschaos', 'chaos', 'chaos-runner', 'hog', 'iostress', 'helper']
    column_list = []
    iteration_list = []
    final_feature_list = []
    checked_shape = False
    dataframes_list = []

    df = pd.read_feather(dataset)

    for column in df.columns:
        if any(keyword in column for keyword in filter_keywords_list) == False:
            iteration_list.append(column)

    return iteration_list


# Function to convert datatypes to numeric.
def convert_datatype(column_data):
    return column_data[0][column_data[1]].apply(pd.to_numeric, errors='coerce')


# Parallelization function.
def modifyParallelized(function, df):
    column_list = [[df, column] for column in df.columns]

    # All the available cores are used.
    cores = mp.cpu_count()

    # Create the multiprocessing pool of cores.
    pool = mp.Pool(cores)

    columns_converted = pool.map(function, column_list)

    # Close down the pool and join.
    pool.close()
    pool.join()
    # pool.clear()

    return columns_converted


def scrap_and_save(job_list):
    for job in job_list:

        if os.path.isfile('data_combined_filtered/' + job + '/' + job + '_filtered.pkl') == True:
            continue

        column_list = []
        iteration_list = []
        final_feature_list = []
        dataframes_list = []

        # Paths for each file are generated.
        dataset_dirs = ['data_scraped/' + job + '/' + file for file in os.listdir('data_scraped/' + job) if job in file]
        dataset_dirs.sort()

        print('Start Job: ' + job)

        df = pd.read_feather(dataset_dirs[0])

        print(df.shape)

        # Any non-litmus job is filtered on litmus related features.
        if job != 'litmuschaos':
            column_list = filter_chaos(dataset_dirs[0])
            df = df[column_list]

        # NA's are dropped, features having more than 5 NA's are dropped.
        df.dropna(axis=1, inplace=True, thresh=(len(df) - 5))
        print(df.shape)
        column_list = [column for column in df.columns if "Timestamp" not in column]
        df[column_list] = pd.concat(modifyParallelized(convert_datatype, df[column_list]), axis=1)

        print(df.shape)

        df.dropna(axis=1, inplace=True, thresh=(len(df) - 5))

        column_list = df.columns
        df.set_index('Timestamp', drop=True, inplace=True)

        # All datasets are merged into one dataset.
        for dataset in dataset_dirs[1:]:
            print(dataset)
            df_concat = pd.read_feather(dataset).set_index('Timestamp', drop=True)
            concat_columns = list(set(column_list).intersection(df_concat.columns))
            df_concat = pd.concat(modifyParallelized(convert_datatype, df_concat[concat_columns]), axis=1)

            df = pd.concat([df, df_concat[concat_columns]], axis=0)
            time.sleep(2)

        print(df.shape)

        # For litmuschaos, only features showing which experiment is running are kept.
        if job == 'litmuschaos':

            df.dropna(axis=0, inplace=True)
            df = df[[column for column in df.columns if 'awaited_experiments' in column]]

        # Final filters are executed.
        else:
            column_list = filter_chaos(dataset_dirs[0])
            df.reset_index(drop=False, inplace=True)
            df.dropna(axis=1, inplace=True, thresh=(len(df) - 5))
            df.dropna(axis=0, inplace=True)
            df = df.loc[:, (df != df.iloc[0]).any()]

        print(df.shape)

        df.to_pickle('data_combined_filtered/' + job + '/' + job + '_filtered.pkl')

        # Feature summary of all jobs.
        for job in job_list:
            df = pd.read_pickle('data_combined_filtered/' + job + '/' + job + '_filtered.pkl')
            print(job)
            print(df.shape)


# Metrics are generated for each dataset based on a 45 second sliding window. They are also saved.
def generate_metrics(job):
    df = pd.read_pickle('data_combined_filtered/' + job + '/' + job + '_filtered.pkl').set_index('Timestamp',
                                                                                                 drop=True).sort_values(
        by='Timestamp')

    dfMetric = df.rolling('45s').mean()
    dfMetric.columns = ['mean_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_mean_' + job + '.pkl')

    dfMetric = df.rolling('45s').quantile(0.05)
    dfMetric.columns = ['quantile05_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_quantile05_' + job + '.pkl')

    dfMetric = df.rolling('45s').quantile(0.25)
    dfMetric.columns = ['quantile25_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_quantile25_' + job + '.pkl')

    dfMetric = df.rolling('45s').quantile(0.50)
    dfMetric.columns = ['quantile50_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_quantile50_' + job + '.pkl')

    dfMetric = df.rolling('45s').quantile(0.75)
    dfMetric.columns = ['quantile75_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_quantile75_' + job + '.pkl')

    dfMetric = df.rolling('45s').quantile(0.95)
    dfMetric.columns = ['quantile95_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_quantile95_' + job + '.pkl')

    dfMetric = df.rolling('45s').var()
    dfMetric.columns = ['variance_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_variance_' + job + '.pkl')

    dfMetric = df.rolling('45s').skew()
    dfMetric.columns = ['skewness_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_skewness_' + job + '.pkl')

    dfMetric = df.rolling('45s').min()
    dfMetric.columns = ['minimum_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_minimum_' + job + '.pkl')

    dfMetric = df.rolling('45s').max()
    dfMetric.columns = ['maximum_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_maximum_' + job + '.pkl')

    dfMetric = df.rolling('45s').kurt()
    dfMetric.columns = ['kurtosis_' + column for column in dfMetric.columns]
    dfMetric.to_pickle('metrics/' + job + '/' + '_kurtosis_' + job + '.pkl')

    return job + ' Done'


def generateParallelized(function, job_list):
    # All the available cores are used.
    cores = mp.cpu_count()

    # Create the multiprocessing pool of cores.
    pool = mp.Pool(cores)

    columns_converted = pool.map(function, job_list)

    # Close down the pool and join.
    pool.close()
    pool.join()
    # pool.clear()

    return columns_converted


def extract_features(job_list):
    # Metrics are generated for each dataset.
    generateParallelized(generate_metrics, [job for job in job_list if job != 'litmuschaos'])
    # Rated features are also saved.
    df = pd.DataFrame()
    for job in [job for job in job_list if job != 'litmuschaos']:
        df_new = pd.read_pickle('data_combined_filtered/' + job + '/' + job + '_filtered.pkl').set_index('Timestamp',
                                                                                                         drop=True)
        df = pd.concat([df, df_new[[column for column in df_new.columns if 'total' in column]]], axis=1)
        print(job)
        print(df_new.shape)

    df.sort_values(by='Timestamp', inplace=True)
    df.columns = ['rated_' + column for column in df.columns]
    df = df.diff()
    df.reset_index(drop=False, inplace=True)
    df.to_pickle('data_combined_filtered/totals-rated/totals-rated_filtered.pkl')
    # Metrics are generated for the rated metrics.
    generateParallelized(generate_metrics, ['totals-rated'])
    # Read litmus data
    df = pd.read_pickle('data_combined_filtered/litmuschaos/litmuschaos_filtered.pkl')
    # Rename litmus features to right experiment names.

    new_names_dict = {
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-node-cpu-hog#default#35.246.222.195:31111#litmuschaos': 'Node CPU hog',
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-node-io-stress#default#35.246.222.195:31111#litmuschaos': 'Node IO stress',
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-node-memory-hog#default#35.246.222.195:31111#litmuschaos': 'Node memory hog',
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-pod-cpu-hog#default#35.246.222.195:31111#litmuschaos': 'Pod CPU hog',
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-pod-io-stress#default#35.246.222.195:31111#litmuschaos': 'Pod IO stress',
        'litmuschaos_awaited_experiments#teastore-chaos#teastore-chaos-pod-memory-hog#default#35.246.222.195:31111#litmuschaos': 'Pod memory hog',
        'litmuschaos_namespace_scoped_awaited_experiments#default#35.246.222.195:31111#litmuschaos': 'Anomalous'}

    df.rename(columns=new_names_dict, inplace=True)
    # All experiments are combined into one multi-class feature.
    conditions = [
        (df['Node CPU hog'] == 1),
        (df['Node IO stress'] == 1),
        (df['Node memory hog'] == 1),
        (df['Pod CPU hog'] == 1),
        (df['Pod IO stress'] == 1),
        (df['Pod memory hog'] == 1),
        (df['Anomalous'] == 0)]

    options = ['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog', 'Pod IO stress', 'Pod memory hog',
               'Non-anomalous']
    options_numbers = [0, 1, 2, 3, 4, 5, 6]
    df['Anomaly_name'] = np.select(conditions, options)
    df['Anomaly_nr'] = np.select(conditions, options_numbers)
    # The changes are saved into a new file.
    df.to_pickle('finalLitmus/litmusfinal.pkl')
