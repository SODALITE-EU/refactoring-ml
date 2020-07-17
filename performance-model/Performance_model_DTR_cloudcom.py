

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt 
from sklearn.tree import DecisionTreeRegressor
from sklearn import linear_model
import seaborn as sns
from utilities import get_N_samplesd
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LinearRegression
from sklearn.feature_selection import RFE
from sklearn.linear_model import RidgeCV, LassoCV, Ridge, Lasso
from sklearn.metrics import mean_squared_error,r2_score, mean_absolute_error
from sklearn.externals.six import StringIO  
from IPython.display import Image  
from sklearn.tree import export_graphviz
import pydotplus

#import data
data = pd.read_csv('Generated Data/final_data.csv', encoding='utf-8')
delete_columns = ['Name','fail_count','Median response time','D11','Hatch Rate','Time', 'variant_cost_hr']
data = data.drop(delete_columns, axis=1)

def get_N_samples(choice, structured_data):
    N1_variants = [1,41,42,44,8,11,20,39,46,33,24,31,50,38,12,17,2]
    N2_variants = [2,6,54, 8, 27, 36, 55, 48, 14, 25, 16, 56,32,35,52,45, 13, 18, 51]
    N3_variants = [3, 5, 7, 10, 15, 21, 23, 27, 28, 29, 37, 43, 47, 49, 53]
    N4_variants = [4, 8, 22, 30, 34]
    
    
    if choice == 'N1':
        N1 = structured_data[structured_data.variant.isin(N1_variants)]
        X_train = N1.iloc[:, 1:15]
        y_train = N1.iloc[:, -1]
        
    elif choice == 'N2':
        N2 = structured_data[structured_data.variant.isin(N1_variants+N2_variants)]
        X_train = N2.iloc[:, 1:15]
        y_train = N2.iloc[:, -1]
        
    elif choice == 'N3':
        N3 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N3_variants)]
        X_train = N3.iloc[:, 1:15]
        y_train = N3.iloc[:, -1]
        
    N4 = structured_data[structured_data.variant.isin(N4_variants)]
    N4_X = N4.iloc[:, 1:15]
    N4_y = N4.iloc[:, -1]
    
    return X_train, y_train, N4_X, N4_y


def get_structured_data(data,label):
    #provides the final structured data for modeling 
    features = data.iloc[:,[x for x in range(9,24)]]
    target = data[label]
    structured_data = pd.concat([features, target], axis=1, sort=False)
    structured_data = remove_duplicates(structured_data)
    return structured_data

def remove_duplicates(data):
    data = data.drop_duplicates(subset=['request category','D1(Small), D2(Medium)', 'D3', 'D4', 'D5(Small)', 'D6(Large)', 'D7',
       'D8', 'D9', 'D10', 'D12(Small), D13(Medium)', 'D14', 'D15', 'D16'])
    return data


# In[123]:


#Train-test split
structured_data =  get_structured_data(data, "mean_rt")


# In[124]:


#Base Model
def base_DTR(structured_data):
    X_train, y_train, X_test, y_test = get_N_samplesd('N1', structured_data)
    base_DTR = DecisionTreeRegressor()
    base_DTR.fit(X_train, y_train)
    #Score
    y_predicted = base_DTR.predict(X_test)
    print('R-Square: ', round(r2_score(y_test, y_predicted),2))
    print('MSE: ', round(mean_squared_error(y_test,y_predicted),2))
    print('MAE: ', round(mean_absolute_error(y_test,y_predicted),2))
    print('RMSE: ', round(mean_squared_error(y_test,y_predicted),2))
base_DTR(structured_data)


# In[64]:


def param_tuningDTR(structured_data):
    #returns the best params
    crit = []
    spli = []
    dep = []
    r2scr = []
    r2scr_nrn = []
    
    X_train, y_train, X_test, y_test = get_N_samplesd('N2', structured_data)


    dept_df = pd.DataFrame()
    dept_df['y'] = y_test.to_list()
 
    criterion = ['mse','friedman_mse','mae']
    for cr in criterion:
        split = ['random', 'best']
        for sp in split:
            depth = [2,4,6,8,10]
            for m in depth:

                mlregr = DecisionTreeRegressor(criterion=cr, splitter=sp, max_depth=m, max_features='auto')
                mlregr.fit(X_train, y_train)

                #Score
                model_score = mlregr.score(X_train,y_train)
                y_predicted = mlregr.predict(X_test)
                crit.append(cr)
                spli.append(sp)
                dep.append(m)
                r2_score_x = r2_score(y_test, y_predicted)
                r2scr.append(r2_score_x)
                dept_df["depth("+str(m)+")"] = y_predicted.tolist()

    history = pd.DataFrame(list(zip(crit, spli, dep, r2scr)), 
                   columns =['criterion', 'splitter', 'max_depth', 'r2_score'])
    best_params = history.iloc[history['r2_score'].idxmax()]
    
    return best_params, dept_df, depth


def trainsize_gridsearch(structured_data, best_params):
  
    scores = []
    mse = []
    mae = []
    rmse = []
    
    sizes = ['N1','N2','N3']
    for size in sizes:
        X_train, y_train, X_test, y_test = get_N_samplesd(size, structured_data)

        mlregr_final = DecisionTreeRegressor(criterion=str(best_params[0]), splitter=best_params[1], max_depth=best_params[2], 
                                             max_features='auto')
        mlregr_final.fit(X_train, y_train)

        #Score
        y_pred = mlregr_final.predict(X_test)
        scores.append(round(r2_score(y_test, y_pred),2))
        mse.append(round(mean_squared_error(y_test, y_pred),2))
        mae.append(round(mean_absolute_error(y_test, y_pred),2))
        rmse.append(round(mean_squared_error(y_test, y_pred, squared=False),2))
        
    size_score = pd.DataFrame(list(zip(sizes, scores, mse, mae, rmse)), columns=['N sample size','r2_score','MSE','MAE','RMSE'])
    
    return size_score

best_params, dept_df, depth = param_tuningDTR(structured_data)
size_score = trainsize_gridsearch(structured_data, best_params)
best_score = size_score.iloc[size_score['r2_score'].idxmax()]
best_train_size = str(best_score[0])
print(size_score)


X_train, y_train, X_test, y_test = get_N_samplesd(best_train_size, structured_data)
mlregr_final = DecisionTreeRegressor(criterion=str(best_params[0]), splitter=best_params[1], max_depth=int(best_params[2]), 
                                         max_features='auto')
mlregr_final.fit(X_train, y_train)

#Score
model_score = mlregr_final.score(X_train, y_train)
y_pred = mlregr_final.predict(X_test)

print("R-squared score: ", round(r2_score(y_test,y_pred),2))
print("MSE: ", round(mean_squared_error(y_test, y_pred),2))
print("MAE: ", round(mean_absolute_error(y_test, y_pred),2))
print("RMSE: ", round(mean_squared_error(y_test, y_pred, squared=False),2))


plt.figure(figsize=(10,6))
ax1 = sns.distplot(dept_df['y'], hist=False, color='r', kde_kws={'linewidth':3},  label="Actual Value", )

for x in depth:
    sns.distplot(dept_df["depth("+str(x)+")"], hist=False, kde_kws={'linestyle':'-.'}, label="Depth "+str(x))
sns.distplot(dept_df["depth("+str(int(best_params[2]))+")"], hist=False, color='b', kde_kws={'linewidth':5}, label="Chosen Depth "+str(int(best_params[2])))
plt.xlabel("Predicted Mean RT")
plt.ylabel("Normalized Range")
plt.title('Prediction accuracy between tree depth')
plt.savefig("Generated Data/DTR_depth.png")
plt.show()



