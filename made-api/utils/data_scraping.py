# Packages are imported.
import datetime as dt
import os
import sys
import time

import numpy as np
import pandas as pd
import requests as req

np.random.seed(1)
sys.setrecursionlimit(25000)


# Function that requests data. Change IP to external IP of one of the VM's collecting data.
def request_data(ip, port, job, startTime, endTime):
    print('Job request: ' + str(job) + ' started.')
    # print(startTime)
    # print(job)
    # print(endTime)
    request = req.get(
        'http://' + ip + ':' + port + '/api/v1/query_range?query={job=~"' + job + '"}&start=' + startTime + 'Z&end=' + endTime + 'Z&step=1s')
    # print(eval(request.content))
    metric_data = pd.DataFrame(eval(request.content))

    return metric_data


# Change IP to external IP of one of the VM's collecting data.
def get_jobs(ip, port):
    # Cluster: Get list of jobs
    x = req.get('http://' + ip + ':' + port + '/api/v1/label/job/values')
    jobList = eval(x.content)['data']

    return jobList


# Get timestamps for next 15mins.
def get_timestamps(date):
    scrape_timeframes = []
    interval = dt.timedelta(minutes=14, seconds=59)

    scrape_timeframes.append([str(date).replace(" ", "T"), str(date + interval).replace(" ", "T"),
                              pd.date_range(date, (date + interval), freq='1S')])

    return scrape_timeframes


# Info of features is saved in separate files.
def save_feature_info(job, dfList_info):
    dfFeatures_info = pd.concat(dfList_info, axis=1)
    dfFeatures_info.columns = [str(column) + dfFeatures_info.columns[column] for column in
                               range(0, len(dfFeatures_info.columns))]
    list_features_info = [dirs for dirs in os.listdir('data_scraped/feature_info/') if job in dirs]
    file_number = len(list_features_info) + 1
    dfFeatures_info.to_feather('data_scraped/feature_info/features_' + job + str(file_number) + '.ftr')


# Function to load in data and structure it into one dataframe.
def structureData(ip, port, job, date):
    scrape_timeframes = get_timestamps(date)
    features_dict = {'Timestamp': scrape_timeframes[0][2]}

    dfMetric = request_data(ip, port, job, scrape_timeframes[0][0], scrape_timeframes[0][1])
    dfFeatures = pd.DataFrame(features_dict)

    print(job + ' metrics: ' + str(len(dfMetric['data'][0])))

    dfList = [pd.DataFrame(metric['values'], columns=['Timestamp', '#'.join(list(metric['metric'].values()))]) for
              metric in dfMetric['data'][0]]
    dfList_info = [pd.DataFrame(metric['metric'].keys(), columns=['#'.join(list(metric['metric'].keys()))]) for metric
                   in dfMetric['data'][0]]
    dfList.insert(0, dfFeatures)

    for df in dfList:
        if len(df.columns) > 1:
            df['Timestamp'] = pd.to_datetime(df['Timestamp'], unit='s')

        else:
            df['Timestamp'] = pd.to_datetime(df['Timestamp'])

    dfList = [df.set_index('Timestamp', drop=True) for df in dfList]

    save_feature_info(job, dfList_info)

    dfFeatures = pd.concat(dfList, axis=1)

    print('Done: ' + job)
    return dfFeatures


# Function that saves the data as feather files.
def save_data(ip, port, job, date):
    tic = time.clock()
    df = structureData(ip, port, job, date)
    file_number = len(os.listdir('data_scraped/' + job + '/')) + 1
    df.reset_index(inplace=True)

    df.to_feather('data_scraped/' + job + '/' + job + str(file_number - 1) + '.ftr')
    toc = time.clock()
    print(job + ': ' + str(toc - tic))

    return df


def scrape_data(ip, port, job_list):
    dfDates = pd.date_range('11:00', '15:45', freq='15min')
    interval = dt.timedelta(minutes=15, seconds=1)
    now = dt.datetime.now()

    for date in dfDates:
        time_passed = False

        while time_passed is False:

            if now > (date + interval):

                time_passed = True

                for job in job_list:
                    df = save_data(ip, port, job, date)
                    time.sleep(5)

            else:
                print('Too early')
                time.sleep(60)

            now = dt.datetime.now()
