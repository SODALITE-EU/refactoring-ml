# Packages are imported.
import multiprocessing as mp
import os
import random
import sys

import numpy as np
import pandas as pd
import statsmodels.stats.multitest as statsmodels
from scipy import stats

np.random.seed(1)
sys.setrecursionlimit(25000)


# Function to calculate p-value for given columns in a df.
def calcPValue(df, column):
    pvalue = stats.ks_2samp(df.loc[df['Anomalous'] == 0, column].values.tolist(),
                            df.loc[df['Anomalous'] == 1, column].values.tolist())[1]

    return pvalue


# Get paths of files.
def getFilePaths():
    directories = []

    for root, dirs, files in os.walk('metrics/', topdown=False):
        for name in files:

            if ('ipynb' not in name) and ('litmus' not in name):
                directories.append(os.path.join(root, name))

    return directories


def calc_p_value(directory):
    dfFeatures = pd.DataFrame(columns=['Feature', 'p_value'])
    df_litmus = pd.read_pickle('finalLitmus/litmusfinal.pkl')
    if 'data_combined_filtered' in directory:
        df_metric = pd.read_pickle(directory).set_index('Timestamp', drop=True)
    else:
        df_metric = pd.read_pickle(directory)

    df_metric = pd.concat([df_metric, df_litmus['Anomalous']], axis=1)
    df_metric.dropna(axis=1, inplace=True, thresh=(len(df_metric) - 20))
    df_metric.dropna(axis=0, inplace=True)

    dfFeatures = pd.DataFrame([(column, calcPValue(df_metric, column)) for column in df_metric.columns if
                               ((column != 'anomaly') and (column != 'time') and (column != 'Anomalous'))],
                              columns=['Feature', 'p_value'])

    return dfFeatures


# Parallelization of p-value calculation.
def pvalue_parallelized(function, directories):
    # All the available cores are used.
    cores = mp.cpu_count()

    # Create the multiprocessing pool of cores.
    pool = mp.Pool(cores)

    # Schrijf hier vstack en pd.df
    feature_list = pd.DataFrame(np.vstack(pool.map(function, directories)), columns=['Features', 'P_value'])

    # Close down the pool and join.
    pool.close()
    pool.join()
    # pool.clear()

    return feature_list


# Function for feature selection.
def select_features(alpha):
    df_pvalue = pd.read_pickle('feature selection/pvalues_onlymetrics.pkl')
    by_results = statsmodels.multipletests(df_pvalue['P_value'], alpha=alpha, method='fdr_by')
    df_pvalue['Rejected'] = by_results[0]
    df_pvalue['P_vals_corrected'] = by_results[1]
    df_filtered = df_pvalue.loc[df_pvalue.Rejected == True]

    return df_filtered


def select_features_all():
    # All feature selection in 1.
    directories = getFilePaths()
    df = pd.read_pickle('finalLitmus/litmusfinal.pkl')

    df_selection = select_features(0.000000000000000000000000000000000000001)
    df_selection.to_pickle('feature selection/features_selected7.pkl')

    df_processed = df[~df.index.isin(random.sample(list(df[df['Anomaly_name'] == 'Non-anomalous'].index), k=9000))]
    column_list = df_selection['Features']

    for file in directories:
        print(file)
        df_new = pd.read_pickle(file)
        if file[:4] == 'data':
            df_new.set_index('Timestamp', inplace=True)

        concat_columns = list(set(column_list).intersection(df_new.columns))
        df_processed = pd.concat([df_processed, df_new[concat_columns]], axis=1)

    df_processed.dropna(axis=0, inplace=True)
    df_processed.drop(
        ['Node CPU hog', 'Node IO stress', 'Node memory hog', 'Pod CPU hog', 'Pod IO stress', 'Pod memory hog',
         'Anomalous'], axis=1, inplace=True)
    df_processed.drop([column for column in df_processed.columns if 'sd_discovered' in column], axis=1, inplace=True)
    df_processed.to_pickle('feature selection/multi-class-final7.pkl')
