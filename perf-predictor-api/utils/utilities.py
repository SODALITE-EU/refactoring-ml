#!/usr/bin/env python
# coding: utf-8

# In[ ]:


import pandas as pd
import numpy as np

import sklearn
from sklearn.model_selection import train_test_split


# In[2]:


def get_N_samplesn(choice, structured_data):
    N1_variants = [1,41,42,44,8,11,20,39,46,33,24,31,50,38,22,12,17,2,4]
    N2_variants = [2,6,54, 8, 27, 36, 55,4,48, 14, 25, 16,35,32,45, 13, 18, 56]
    N3_variants = [3, 5, 8, 10, 15, 22, 23, 27, 28, 29, 30, 43, 47, 4, 56]
    N4_variants = [4, 8, 22, 30, 34, 56, 32]
    
    
    
    if choice == 'N1':
        N1 = structured_data[structured_data.variant.isin(N1_variants)]
        X = N1.iloc[:, 1:15]
        y = N1.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1)   
        
    elif choice == 'N2':
        N2 = structured_data[structured_data.variant.isin(N1_variants+N2_variants)]
        X = N2.iloc[:, 1:15]
        y = N2.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1)
        
    elif choice == 'N3':
        N3 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N3_variants)]
        X = N3.iloc[:, 1:15]
        y = N3.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.1)
        
    N4 = structured_data[structured_data.variant.isin(N4_variants)]
    N4_X = N4.iloc[:, 1:15]
    N4_y = N4.iloc[:, -1]
    
    return X_train, y_train, N4_X, N4_y


# In[4]:


def get_N_samplesr(choice, structured_data):
    N1_variants = [1,41,42,44,8,11,20,39,46,33,24,31,50,38,12,17,2]
    N2_variants = [2,6,54, 8, 27, 36, 55, 48, 14, 25, 16, 56,32,35,52,45, 13, 18, 51]
    N3_variants = [3, 5, 7, 10, 15, 21, 23, 27, 28, 29, 37, 43, 47, 49, 53]
    N4_variants = [4, 8, 22, 30, 34]
    if choice == 'N1':
        N1 = structured_data[structured_data.variant.isin(N1_variants+N4_variants)]
        X = N1.iloc[:, 1:15]
        y = N1.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.6)   
        
    elif choice == 'N2':
        N2 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N4_variants)]
        X = N2.iloc[:, 1:15]
        y = N2.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.75)
        
    elif choice == 'N3':
        N3 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N3_variants+N4_variants)]
        X = N3.iloc[:, 1:15]
        y = N3.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.6)
        
    N4 = structured_data[structured_data.variant.isin(N4_variants)]
    N4_X = N4.iloc[:, 1:15]
    N4_y = N4.iloc[:, -1]
    
    return X_train, y_train, N4_X, N4_y


# In[5]:


def get_N_samplesd(choice, structured_data):
    N1_variants = [1,41,42,44,8,11,20,39,46,33,24,31,50,38,12,17,2]
    N2_variants = [2,6,54, 8, 27, 36, 55, 48, 14, 25, 16, 56,32,35,52,45, 13, 18, 51]
    N3_variants = [3, 5, 7, 10, 15, 21, 23, 27, 28, 29, 37, 43, 47, 49, 53]
    N4_variants = [4, 8, 22, 30, 34]
    
    #DTR
    if choice == 'N1':
        N1 = structured_data[structured_data.variant.isin(N1_variants+N4_variants)]
        X = N1.iloc[:, 1:15]
        y = N1.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.5)   
        
    elif choice == 'N2':
        N2 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N4_variants)]
        X = N2.iloc[:, 1:15]
        y = N2.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.75)
        
    elif choice == 'N3':
        N3 = structured_data[structured_data.variant.isin(N1_variants+N2_variants+N3_variants+N4_variants)]
        X = N3.iloc[:, 1:15]
        y = N3.iloc[:, -1]
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.6)
        
    N4 = structured_data[structured_data.variant.isin(N4_variants)]
    N4_X = N4.iloc[:, 1:15]
    N4_y = N4.iloc[:, -1]
    
    return X_train, y_train, N4_X, N4_y


# In[ ]:




