#!/usr/bin/env python
# coding: utf-8

# In[2]:


import pandas as pd
import numpy as np
import matplotlib.pyplot as plt 


# In[78]:


#load Data
#R2 comparison for train set sizes
RFR_score = pd.read_csv('Generated Data/RFR_score.csv')
RFR_score = RFR_score.rename(columns={"N Samples size": "Sample_set","R-Sqaured": "RFR_r2_score"})
DTR_score = pd.read_csv('Generated Data/DTR_score.csv')
DTR_score = DTR_score.rename(columns={"N sample size": "Sample_set", "r2_score": "DTR_r2_score"})
NN_score = pd.read_csv('Generated Data/NN_score_new.csv')
NN_score = NN_score.rename(columns={"train_size_features": "Sample_set", "r2_score": "NN_r2_score"})


# In[79]:


scores = pd.DataFrame([RFR_score.RFR_r2_score, DTR_score.DTR_r2_score, NN_score.NN_r2_score,]).transpose()
sample = np.array([1,2,3])


# In[82]:


size_fig = plt.figure(figsize=(16,10))
plt.plot(sample, scores.RFR_r2_score.to_numpy(), '-s', markerfacecolor='black', label = "RFR", linewidth=4)
plt.plot(sample, scores.DTR_r2_score.to_numpy(), '-s', markerfacecolor='black', label = "DTR", linewidth=4)
plt.plot(sample, scores.NN_r2_score.to_numpy(), '-s', markerfacecolor='black', label = "NN", linewidth=4)
# plt.grid( linestyle='-', linewidth='0.1', color='grey')
my_xticks = ['N', 'N2', 'N3']
plt.xlabel('Train size (%)')
plt.ylabel('R-squared score')

plt.title('Train size impact on each models')
plt.xticks(sample, my_xticks)
plt.legend()
#plt.savefig("Generated Data/size_accuracy_final.png")
plt.show()


# In[ ]:




