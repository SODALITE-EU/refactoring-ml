import tensorflow as tf
from IPython import get_ipython
from keras.models import Sequential
from keras.layers import Dense
from keras.optimizers import Adam
from keras.callbacks import EarlyStopping

import pandas as pd
import numpy as np

import sklearn
from sklearn import preprocessing
from sklearn.model_selection import train_test_split
from sklearn.metrics import r2_score, mean_squared_error, mean_absolute_error

import seaborn as sns
from matplotlib import pyplot as plt
# get_ipython().run_line_magic('matplotlib', 'inline')
# get_ipython().run_line_magic('config', "InlineBackend.figure_format='retina'")


def get_structured_data(data,label):
    #provides the final structured data for modeling 
    features = data.iloc[:,[x for x in range(10,24)]]
    target = data[label]
    structured_data = pd.concat([features, target], axis=1, sort=False)
    #structured_data = remove_duplicates(structured_data)
    return structured_data

def remove_duplicates(data):
    data = data.drop_duplicates(subset=['request category','D1(Small), D2(Medium)', 'D3', 'D4', 'D5(Small)', 'D6(Large)', 'D7',
       'D8', 'D9', 'D10', 'D12(Small), D13(Medium)', 'D14', 'D15', 'D16'])
    return data


# In[36]:


#import data
data = pd.read_csv('Generated Data/final_data.csv', encoding='utf-8')
delete_columns = ['Name','fail_count','Median response time','D11','Hatch Rate','Time', 'variant_cost_hr']
data = data.drop(delete_columns, axis=1)
structured_data =  get_structured_data(data, "cumulative_moving_avg_rt")
#pd.read_csv('Generated Data/train.csv')

# train = pd.read_csv('Generated Data/train.csv')
# train = train.drop(delete_columns, axis=1)
# train = get_structured_data(train,"cumulative_moving_avg_rt")
# train = train.sample(frac=1).reset_index(drop=True)

# test = pd.read_csv('Generated Data/test.csv')
# test = test.drop(delete_columns, axis=1)
# test = get_structured_data(test,"cumulative_moving_avg_rt")
# test = test.sample(frac=1).reset_index(drop=True)
# #split 
# X_train = train.iloc[:, 0:14]  #Feature Matrix
# y_train = train.iloc[:, -1]

# X_test = test.iloc[:, 0:14] 
# y_test = test.iloc[:, -1]
# X_train.shape


#Train-test split
X = structured_data.iloc[:, 0:14]  #Feature Matrix
Y = structured_data.iloc[:, -1] #Target Variable


def deep_layer_neurons(X, Y, sizes):
    # perfroms grid search to find best neurons for hidden layers
    scores = []
    neur = []

    for size in sizes:
        X_train, X_test, y_train, y_test = train_test_split(X, Y, test_size=size, random_state=42)

        neurons = pd.DataFrame()
        neurons['y_test'] = y_test.to_list()
        neuron_cnt = []
        r2_sco = []
        all_units = [8, 16, 32,64,128,256]

        for un in all_units:
            model = Sequential()
            model.add(Dense(un, input_shape=(14,), kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(un, kernel_initializer='normal', activation='relu'))
            model.add(Dense(1, kernel_initializer='normal', activation='linear'))
            model.compile(Adam(lr=0.001), 'mean_squared_error')

            history = model.fit(X_train, y_train, epochs=200, validation_split=0.2, shuffle=False, verbose=0,
                                batch_size=100)

            y_test_pred = model.predict(X_test)
            neurons["(" + str(un) + ") neurons"] = y_test_pred.flatten().tolist()

            neuron_cnt.append(un)
            r2_sco.append(r2_score(y_test, y_test_pred))

        param_hist = pd.DataFrame(list(zip(neuron_cnt, r2_sco)), columns=['neurons', 'r2_sco'])
        best_params = param_hist.iloc[param_hist['r2_sco'].idxmax()]
        units = int(best_params[0])
        neur.append(units)
        scores.append(round(r2_score(y_test, y_test_pred), 2))

    best_scores = pd.DataFrame(list(zip([1 - s for s in sizes], scores, neur)),
                               columns=['train_size', 'r2_score', 'neurons'])
    # best = best_scores.iloc[best_scores['r2_score'].idxmax()]

    # Return the accuracies (best_scores) at every neuron size and number of best neurons(units) and train size
    return units, best_scores, neurons, sizes, all_units

#Perform grid search and get optimal params
train_sizes = [0.1,0.2,0.3,0.4,0.5]
#Perform grid search and get optimal params
#train_sizes = [0.4,0.5]
units, best_scores, neurons, sizes, all_units = deep_layer_neurons(X,Y, train_sizes)
best = best_scores.iloc[best_scores['r2_score'].idxmax()]
best_train_size = round(1-best[0],2)
best_neurons = int(best[2])

#Optimal NN model
XX_train, XX_test, yy_train, yy_test = train_test_split(X, Y, test_size=best_train_size, random_state=42)

model_final = Sequential()
model_final.add(Dense(units,  input_shape=(14,), kernel_initializer='normal', activation='relu'))
model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
model_final.add(Dense(units, kernel_initializer='normal', activation='relu'))
model_final.add(Dense(1, kernel_initializer='normal', activation='linear'))
model_final.compile(Adam(lr=0.001), 'mean_squared_error')

model_final.fit(XX_train, yy_train, epochs = 200, validation_split = 0.2, shuffle = False, verbose = 0, batch_size=100)

y_pred = model_final.predict(XX_test)

print("The R2 score with ("+str(units)+") neurons is:\t{:0.3f}".format(r2_score(yy_test, y_pred)))
print("MSE: ", round(mean_squared_error(yy_test, y_pred),3))
print("MAE: ", round(mean_absolute_error(yy_test, y_pred),3))
#print("The MSE  is:\t{:0.3f}".format(mean_squared_error(yy_test, y_pred)))
#Visualize the NN Model layers and dimensions
# tf.keras.utils.plot_model(
#     model_final,
#     #to_file="Generated Data/NN_model_layers.png",
#     show_shapes=True,
#     show_layer_names=False,
#     rankdir="LR",
#     expand_nested=False,
#     dpi=150)


plt.figure(figsize=(10,6))
ax1 = sns.distplot(neurons['y_test'], hist=False, color='r', kde_kws={'linewidth':5},  label="Actual Value", )

for x in all_units:
    sns.distplot(neurons["("+str(x)+") neurons"], kde_kws={'linestyle':'--'}, hist=False,  label="("+str(x)+") neurons")
sns.distplot(neurons["("+str(units)+") neurons"], hist=False, color='b', kde_kws={'linewidth':3}, label="Chosen Neurons "+str(units))
plt.xlabel("Predicted moving Mean RT")
plt.ylabel("")
plt.title('Prediction accuracy at each neuron density')
plt.show()
#plt.savefig("Generated Data/NN_accuracy.png")

size_fig = plt.figure(figsize=(10,6))
plt.plot(best_scores.train_size.values*100, best_scores.r2_score, '-s', label = "NN", )
plt.grid( linestyle='-', linewidth='0.5', color='green')
plt.xlabel('Train size (%)')
plt.ylabel('R-squared score')
plt.title('Train size impact on Accuracy')
plt.legend()
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

