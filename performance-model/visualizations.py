import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

# data = pd.read_csv("D://Blogs//train.csv")
# X = data.iloc[:,0:20]  #independent columns
# y = data.iloc[:,-1]    #target column i.e price range
# #get correlations of each features in dataset


#import data
data = pd.read_csv('Generated Data/final_data.csv', encoding='utf-8')
delete_columns = ['Name','fail_count','Median response time','D11','Hatch Rate','Time', 'variant_cost_hr']
data = data.drop(delete_columns, axis=1)

def get_structured_data(data,label):
    #provides the final structured data for modeling
    features = data.iloc[:,[x for x in range(10,24)]]
    target = data[label]
    structured_data = pd.concat([features, target], axis=1, sort=False)
    structured_data = remove_duplicates(structured_data)
    return structured_data

def remove_duplicates(data):
    data = data.drop_duplicates(subset=['request category','D1(Small), D2(Medium)', 'D3', 'D4', 'D5(Small)', 'D6(Large)', 'D7',
       'D8', 'D9', 'D10', 'D12(Small), D13(Medium)', 'D14', 'D15', 'D16'])
    return data

#Train-test split (coose between fixed mean or moving mean responce time)
structured_data =  get_structured_data(data, "mean_rt")
#structured_data =  get_structured_data(data, "cumulative_moving_avg_rt")

corrmat = structured_data.corr()
top_corr_features = corrmat.index
plt.figure(figsize=(12,12))
#plot heat map
g=sns.heatmap(structured_data[top_corr_features].corr(),annot=True,cmap="BuPu",  fmt='.2g')
plt.show()