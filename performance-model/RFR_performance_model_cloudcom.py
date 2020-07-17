import pandas as pd
import numpy as np
from sklearn import linear_model
import statsmodels.api as sm
import matplotlib.pyplot as plt 
import seaborn as sns
from utilities import get_N_samplesr
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error,r2_score, mean_absolute_error
from sklearn.linear_model import LinearRegression
from sklearn.ensemble import RandomForestRegressor


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


# In[109]:


#import data
data = pd.read_csv('Generated Data/final_data.csv', encoding='utf-8')
delete_columns = ['Name','fail_count','Median response time','D11','Hatch Rate','Time', 'variant_cost_hr']
data = data.drop(delete_columns, axis=1)

#Train-test split
structured_data =  get_structured_data(data, "mean_rt")


# In[110]:


#Baseline Model
def base_RFR(structured_data):
    X_train, y_train, N4_X, N4_y = get_N_samplesr('N1', structured_data)
    base_RFR = RandomForestRegressor()
    base_RFR.fit(X_train, y_train)
    y_pred = base_RFR.predict(N4_X)
    r2 = r2_score(N4_y,y_pred)
    print('R-Square: ', r2)
    print('MSE: ', mean_squared_error(N4_y,y_pred))
    print('MAE: ', mean_absolute_error(N4_y,y_pred))
    print('RMSE', mean_squared_error(N4_y,y_pred, squared=False))
    
    
print(base_RFR(structured_data))


def perfrom_RFR_GridSearch(structured_data, trees, sizes):    
    scores = []
    best_tree = []
    best_feat = []
    bst_mse = []
    bst_mae = []
    bst_rmse = []

    for size in sizes:
        param_hist = pd.DataFrame()

        mx_feat = []
        n_trees = []
        r2_sc = []
        mse = []
        mae = []
        rmse = []
        X_train, y_train, N4_X, N4_y = get_N_samplesr(size, structured_data)

        for x in trees:
            for d  in range(1,15):
                rnd_forest_Reg = RandomForestRegressor(n_estimators=x, max_features=d)
                rnd_forest_Reg.fit(X_train, y_train)
                y_pred = rnd_forest_Reg.predict(N4_X)
                r2 = r2_score(N4_y,y_pred)
                n_trees.append(x)
                mx_feat.append(d)
                r2_sc.append(r2)
                mse.append(round(mean_squared_error(N4_y,y_pred),2))
                mae.append(round(mean_absolute_error(N4_y,y_pred),2))
                rmse.append(round(mean_squared_error(N4_y,y_pred, squared=False),2))

        param_hist = pd.DataFrame(list(zip(n_trees, mx_feat, r2_sc, mse, mae, rmse)), columns =['n_estimators', 'max_features', 'r2_score','MSE','MAE','RMSE'])
        best_params = param_hist.iloc[param_hist['r2_score'].idxmax()]

        best_tree.append(int(best_params[0]))
        best_feat.append(int(best_params[1]))
        scores.append(round(best_params[2],2))
        bst_mse.append(best_params[3])
        bst_mae.append(best_params[4])
        bst_rmse.append(best_params[5])

    size_score = pd.DataFrame(list(zip(sizes, scores, best_tree, best_feat, bst_mse, bst_mae, bst_rmse)), columns=['N Samples size','R-Sqaured','No.Trees','Max Features','MSE','MAE','RMSE'])
    
    return size_score

trees = [100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000]
sizes = ['N1','N2','N3']
size_score = perfrom_RFR_GridSearch(structured_data, trees, sizes)

print(size_score)

best = size_score.iloc[size_score['R-Sqaured'].idxmax()]
#bes_n_est = int(best[2])
best_mx_feat = int(best[3])
best_train_size = best[0]
def get_tree(best_train_size,structured_data,best_mx_feat):
    X_train, y_train, X_test, y_test = get_N_samplesr(best_train_size, structured_data)
    n_trees = [100, 200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000]
    tree_df = pd.DataFrame()
    tree_df['y'] = y_test.to_list()
    r2 = []
    for x in n_trees:

        rfReg = RandomForestRegressor(n_estimators=x, max_features=best_mx_feat)
        rfReg.fit(X_train, y_train)
        y_pred = rfReg.predict(X_test)

        tree_df["("+str(x)+")trees"] = y_pred.tolist()
        r2.append(round(r2_score(y_test,y_pred),3))
        
#         print("R2 score: ",round(r2_score(y_test,y_pred),3))
#         print("R2 score: ",round(mean_squared_error(y_test,y_pred),3))
        tree_score = pd.DataFrame(list(zip(r2, n_trees)), columns=['R-Sqaured','No.Trees'])
    return tree_df, tree_score
tree_df, tree_score = get_tree(best_train_size,structured_data,best_mx_feat)
bst_tree = tree_score.iloc[tree_score['R-Sqaured'].idxmax()]


plt.figure(figsize=(10,6))
ax1 = sns.distplot(tree_df['y'], hist=False, color='r', kde_kws={'linewidth':3},  label="Actual Value", )

for x in trees:
    sns.distplot(tree_df["("+str(x)+")trees"], hist=False, kde_kws={'linestyle':'-.'}, label="("+str(x)+") Trees")
sns.distplot(tree_df["("+str(int(bst_tree[1]))+")trees"], hist=False, color='b', kde_kws={'linewidth':5}, label="("+str(int(bst_tree[1]))+") Trees")
plt.xlabel("Predicted Mean RT")
plt.ylabel("Normalized Range")
plt.title('Prediction accuracy between number of Trees')
#plt.savefig("Generated Data/RFR_depth.png")
plt.show()




