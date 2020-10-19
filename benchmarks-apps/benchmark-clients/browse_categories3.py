# -*- coding: utf-8 -*-
"""
Created on Sun Dec  1 16:38:33 2019

@author: Hameez
"""

from locust import HttpLocust, TaskSet, task, between
import random
from random import randrange, choice



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
        region_lst = [4,7,10,13,16,19,22,25,28,31,37,40,58,61,64,70,79]
        region = str(random.choice(region_lst))

        self.client.post("/SearchItemsByCategory.php?category="+region+"&categoryName="+str(random.choice(categories_list))+"+")


class WebsiteUser(HttpLocust):
    task_set = UserBehavior

    wait_time = between(5, 10)
    host = "http://35.202.88.190/PHP"
