import os

import pandas as pd

path_new = 'benchmarkdata/Data_2/'
master_data_path = 'benchmarkdata/Data variannts.xlsx'
path_old = 'benchmarkdata/Data_1/'
generated_path = 'results/'
delete_columns_new = ['Type', 'Average Content Size', 'Requests Failed/s', '50%', '66%',
                      '75%', '80%', '90%', '95%', '98%', '99%', '99.9%', '99.99%', '99.999', '100%']
delete_columns_old = ['Method', 'Average Content Size']
rename_colums = {'# requests': 'request_count', '# failures': 'fail_count'}
variant_data = pd.read_excel(master_data_path, sheet_name='Variants', index_col=None)

# Calculate deployment variant cost using this scheme
vm_price_scheme = {
    "D1(Small), D2(Medium)": 0.1425,
    "D3": 0.19,
    "D4": 0.38,
    "D5(Small)": 0.2375,
    "D6(Large)": 0.76,
    "D7": 0.095,
    "D8": 0.19,
    "D9": 0.95,
    "D10": 0.19,
    "D11": 0.38,
    "D12(Small), D13(Medium)": 0.1425,
    "D14": 0.19,
    "D15": 0.095,
    "D16": 0.38
}


def csv_files_to_df_new(path, delete_columns):
    # Takes a path to folder and merges all the files in that folder to a dataframe
    # list of columns to drop in the dataframe
    files = os.listdir(path)
    files_xls = [f for f in files if f[-9:] == 'stats.csv']
    unclean_df = pd.DataFrame()
    for f in files_xls:
        data = pd.read_csv(path + f)
        data = add_rt_calculations(data)
        row_len = len(data)
        data = data.drop(delete_columns, axis=1)
        variant = f.split('_')[0]
        variant = variant[7:9]
        request_count = f.split('_')[1]
        var_lst = [variant] * row_len
        request_range = [request_count] * row_len
        data["variant"] = var_lst
        data["request category"] = request_range
        unclean_df = unclean_df.append(data)
    return unclean_df


def csv_files_to_df_old(path, delete_columns):
    # Takes a path to folder and merges all the files in that folder to a dataframe
    # list of columns to drop in the dataframe
    files = os.listdir(path)
    files_xls = [f for f in files if f[-8:] == 'ests.csv']
    unclean_df = pd.DataFrame()
    for f in files_xls:
        data = pd.read_csv(path + f)
        data = add_rt_calculations(data)
        row_len = len(data)
        data = data.drop(delete_columns, axis=1)
        variant = f.split('_')[0]
        variant = variant[7:9]
        request_count = f.split('_')[1]
        var_lst = [variant] * row_len
        request_range = [request_count] * row_len
        data["variant"] = var_lst
        data["request category"] = request_range
        unclean_df = unclean_df.append(data)
    return unclean_df


def get_merged_df(path_new, path_old, del_new, del_old, variant_set, col_name):
    # merge both old and new dataframe
    df_old = csv_files_to_df_old(path_old, del_old)
    # df_old = add_rt_calculations(df_old)
    df_new = csv_files_to_df_new(path_new, del_new)
    # df_new = add_rt_calculations(df_new)
    merged_lst = [df_old, df_new]
    con_df = pd.concat(merged_lst)
    convert_dict = {'variant': 'int64', 'request category': 'int64'}
    con_df = con_df.astype(convert_dict)
    merged_df = con_df.merge(variant_set, on=['variant', 'request category'])
    merged_df = merged_df.rename(columns=col_name)
    return merged_df


def add_rt_calculations(test_data):
    # Adds moving and static Mean() and Std() for the average responce time
    test_data = test_data[test_data.Name != "Aggregated"]
    test_data['mean_rt'] = test_data['Average response time'].mean()
    test_data['cumulative_moving_avg_rt'] = test_data['Average response time'].expanding(min_periods=5).mean()
    test_data['std_rt'] = test_data['Average response time'].std()
    test_data['moving_std_rt'] = test_data['Average response time'].expanding(min_periods=5).std()
    test_data.dropna(inplace=True)
    cols = ['mean_rt', 'cumulative_moving_avg_rt', 'std_rt', 'moving_std_rt']
    test_data[cols] = test_data[cols].astype('int64')
    return test_data


def add_vm_cost(data, cost_structure):
    # this method calculates the total cost of a deployment for the specific time (30 mins)
    final_cost_hrly = []
    for index, rows in data.iloc[:, 1:15].iterrows():
        row_cost = []
        for name, items in rows.iteritems():
            if items == 1:
                cst = cost_structure.get(name)
                row_cost.append(cst)
            else:
                row_cost.append(0)
        final_cost_hrly.append(sum(row_cost))

    data["variant_cost_hr"] = final_cost_hrly
    final_cost_30m = [element * 0.5 for element in final_cost_hrly]
    data["variant_cost_30m"] = [round(num, 3) for num in final_cost_30m]

    return data


######## Main ######
variant_data_with_cost = add_vm_cost(variant_data, vm_price_scheme)
data = get_merged_df(path_new, path_old, delete_columns_new, delete_columns_old, variant_data_with_cost, rename_colums)
data.to_csv(generated_path + 'final_data.csv', encoding='utf-8', index=False)
