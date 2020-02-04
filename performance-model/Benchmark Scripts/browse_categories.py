# -*- coding: utf-8 -*-
"""
Created on Sun Dec  1 16:38:33 2019

@author: Hameez
"""

from locust import HttpLocust, TaskSet, task
from random import randrange
import random










class UserBehavior(TaskSet):
    
    global categories_list 

#    @task
#    def browse(self):
#        
#        self.client.get("/")
#   
    @task
    def searcgbyCtegory(self):
        
        categories_list = ["Antiques+%26+Art","Books","Business%2C+Office+%26+Industrial","Clothing+%26+Accessories","Coins","Computers"]

        region = str(randrange(start=0, stop=61))
        self.client.post("/SearchItemsByCategory.php?category="+region+"&categoryName="+str(random.choice(categories_list))+"+")

        
    
        




class WebsiteUser(HttpLocust):
    task_set = UserBehavior
    
    min_wait = 1000
    max_wait = 10000
    host = "http://35.222.217.191/PHP"