
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam
from keras.callbacks import EarlyStopping
from utilities import get_N_samplesn
import pandas as pd
import numpy as np

import sklearn
from sklearn import preprocessing
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score, mean_squared_error, mean_absolute_error

import seaborn as sns
from matplotlib import pyplot as plt

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
    #structured_data = remove_duplicates(structured_data)
    return structured_data

def remove_duplicates(data):
    data = data.drop_duplicates(subset=['request category','D1(Small), D2(Medium)', 'D3', 'D4', 'D5(Small)', 'D6(Large)', 'D7',
       'D8', 'D9', 'D10', 'D12(Small), D13(Medium)', 'D14', 'D15', 'D16'])
    return data

#import data
data = pd.read_csv('Generated Data/final_data.csv', encoding='utf-8')
delete_columns = ['Name','fail_count','Median response time','D11','Hatch Rate','Time', 'variant_cost_hr']
data = data.drop(delete_columns, axis=1)
structured_data =  get_structured_data(data, "cumulative_moving_avg_rt")


def deep_layer_neurons(structured_data,sizes):
    #perfroms grid search to find best neurons for hidden layers   
    scores = []
    neur = []
    bst_mse = []
    bst_mae = []
    bst_rmse = []

    for size in sizes:
        X_train, y_train, N4_X, N4_y = get_N_samplesn(size, structured_data)

        neurons = pd.DataFrame()
        neurons['y_test'] = N4_y.to_list()
        neuron_cnt = []
        r2_sco = []
        all_units = [16,32,64,128]
        mse = []
        mae = []
        rmse = []

        for un in all_units:
            model = Sequential()
            model.add(Dense(un,  input_shape=(14,), kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(1, kernel_initializer='normal', activation='linear'))
            model.compile(Adam(lr=0.001), 'mean_squared_error')

            history = model.fit(X_train, y_train, epochs = 200, validation_split = 0.2, shuffle = False, verbose = 0, batch_size=100)

            y_test_pred = model.predict(N4_X)
            neurons["("+str(un)+") neurons"] = y_test_pred.flatten().tolist()

            neuron_cnt.append(un)
            r2_sco.append(round(r2_score(N4_y, y_test_pred),2))
            mse.append(round(mean_squared_error(N4_y,y_test_pred),2))
            mae.append(round(mean_absolute_error(N4_y,y_test_pred),2))
            rmse.append(round(mean_squared_error(N4_y,y_test_pred, squared=False),2))

        param_hist = pd.DataFrame(list(zip(neuron_cnt, r2_sco, mse, mae, rmse)), columns =['neurons', 'r2_sco','MSE','MAE','RMSE'])
        best_params = param_hist.iloc[param_hist['r2_sco'].idxmax()]
        units = int(best_params[0])
        neur.append(units)
        scores.append(best_params[1])
        bst_mse.append(best_params[2])
        bst_mae.append(best_params[3])
        bst_rmse.append(best_params[4])

    best_scores = pd.DataFrame(list(zip(sizes, scores, neur, bst_mse, bst_mae, bst_rmse)), columns=['train_size_features','r2_score','neurons','MSE','MAE','RMSE'])
    #best = best_scores.iloc[best_scores['r2_score'].idxmax()]
    
    
    #Return the accuracies (best_scores) at every neuron size and number of best neurons(units) and train size 
    return units, best_scores, neurons, sizes, all_units


train_sizes = ['N1','N2','N3']
units, best_scores, neurons, sizes, all_units = deep_layer_neurons(structured_data, train_sizes)
print(best_scores)

# #Perform grid search and get optimal params
# train_sizes = [0.4,0.5]
# units, best_scores, neurons, sizes, all_units = deep_layer_neurons(X,Y, train_sizes)
# best = best_scores.iloc[best_scores['r2_score'].idxmax()]
# best_train_size = 1-best[0]
# best_neurons = int(best[2])

# #Optimal NN model
# XX_train, XX_test, yy_train, yy_test = train_test_split(X, Y, test_size=best_train_size, random_state=42)

# model_final = Sequential()
# model_final.add(Dense(units,  input_shape=(14,), kernel_initializer='normal', activation='relu'))
# model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
# model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
# model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
# model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
# model_final.add(Dense(1, kernel_initializer='normal', activation='linear'))
# model_final.compile(Adam(lr=0.001), 'mean_squared_error')

# model_final.fit(XX_train, yy_train, epochs = 200, validation_split = 0.2, shuffle = False, verbose = 0, batch_size=100)

# y_pred = model_final.predict(XX_test)

# print("The R2 score with ("+str(units)+") neurons is:\t{:0.3f}".format(r2_score(yy_test, y_pred)))
# #print("The MSE  is:\t{:0.3f}".format(mean_squared_error(yy_test, y_pred)))      


# In[29]:


# tf.keras.utils.plot_model(
#     model_final,
#     #to_file="NN_model_layers.png",
#     show_shapes=True,
#     show_layer_names=False,
#     rankdir="LR",
#     expand_nested=False,
#     dpi=150, 
# )


# In[62]:


plt.figure(figsize=(16,10))
ax1 = sns.distplot(neurons['y_test'], hist=False, color='r', kde_kws={'linewidth':5},  label="Actual Value", )

for x in all_units:
    sns.distplot(neurons["("+str(x)+") neurons"], kde_kws={'linestyle':'--'}, hist=False,  label="("+str(x)+") neurons")
sns.distplot(neurons["("+str(units)+") neurons"], hist=False, color='b', kde_kws={'linewidth':3}, label="Chosen Neurons "+str(units))
plt.xlabel("Predicted moving Mean RT")
plt.ylabel("Normalized Range")
plt.title('Prediction accuracy at each neuron density')
#plt.savefig("Generated Data/NN_accuracy_new.png")
plt.show()

# sizes = [0.1,0.2,0.3,0.4,0.5]
# scores = []
# for size in sizes:
#     param_hist = pd.DataFrame()
    
#     XX_train, XX_test, yy_train, yy_test = train_test_split(X, Y, test_size=size, random_state=42)
    
#     model_final = Sequential()
#     model_final.add(Dense(units,  input_shape=(14,), kernel_initializer='normal', activation='relu'))
#     model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
#     model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
#     model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
#     model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
#     model_final.add(Dense(1, kernel_initializer='normal', activation='linear'))
#     model_final.compile(Adam(lr=0.001), 'mean_squared_error')

#     # Fits model over 2000 iterations with 'earlystopper' callback, and assigns it to history
#     model_final.fit(XX_train, yy_train, epochs = 200, validation_split = 0.2, shuffle = False, verbose = 0, batch_size=100)

#     y_pred = model_final.predict(XX_test)
#     #print("The R2 score is:\t{:0.3f}".format(r2_score(y_test, y_pred)))
#     scores.append(round(r2_score(yy_test, y_pred),2))

# size_score = pd.DataFrame(list(zip([1-s for s in sizes], scores)), columns=['train_size','r2_score'])

