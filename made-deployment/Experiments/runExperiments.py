import os
import sys
import time
import random

#os.system('kubectl apply -f adminrbac.yml -n default')
experimentCategories = []
#random.shuffle(experimentCategories)

#print(experimentCategories)
i=0
limit = 10000
while i < limit:  
    
    
    if not experimentCategories:

        
        experimentCategories = [x[0] for x in os.walk(os.path.dirname(os.path.realpath(__file__))) if 'templates' not in x[0] if 'del' not in x[0]][1:]
        print(experimentCategories)
        #print(experimentCategories)
        
        #experimentCategories = [x[0] for x in os.walk(r'D:\Master thesis\Experimental procedure\LitmusExperiments\testex')][1:]
        
        i=i+1
        print("Iteration: " + str(i))
        
        if i == limit:
            break
    
    
    
    choiceExperiment = random.randint(0, len(experimentCategories)-1)
    
    #print(choiceExperiment)
    print(experimentCategories[choiceExperiment])

    
    experiments = os.listdir(experimentCategories[choiceExperiment])

    

    

    choiceExperimentConfig = random.randint(0, len(experiments)-1)

    #experimentConfig = experimentCategories[choiceExperiment] + "//" + experiments[choiceExperimentConfig]
    experimentConfig = os.path.join(experimentCategories[choiceExperiment], experiments[choiceExperimentConfig])
    #print(experimentConfig)

    

    del experimentCategories[choiceExperiment]

    print("Choice: " + experiments[choiceExperimentConfig])

    os.system('kubectl apply -f ' + experimentConfig + ' -n default')
    
    sleepTime = random.randint(90, 110)
    
    time.sleep(sleepTime)
    
    #print(i)
    #os.system('kubectl apply -f pod-memhog.yml -n default')
    #i=i+1
    #time.sleep(100)
    #os.system('kubectl apply -f pod-cpuhog.yml -n default')

print('Done')