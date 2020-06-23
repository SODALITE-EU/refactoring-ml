import os

variant_01 = 38
variant_02 = 39
variant_03 = 40
variant = [100,500,1000,2000,3000]
#varient_temp = [500,1000,2000,3000]

for val in variant:
    os.system('locust -f "E:/JADS/Thesis/Experiment/Benchmark/browse_categories.py" --csv="E:/JADS/Thesis/Experiment/Data Source/Data_1/variant"'+str(
        variant_01)+'"_"'+str(val)+' --no-web -c '+str(val)+' -r 100 --run-time 30m')

for val in variant:
    os.system(
        'locust -f "E:/JADS/Thesis/Experiment/Benchmark/browse_categories2.py" --csv="E:/JADS/Thesis/Experiment/Data Source/Data_1/variant"' + str(
            variant_02) + '"_"' + str(val) + ' --no-web -c ' + str(val) + ' -r 100 --run-time 30m')

for val in variant:
    os.system(
        'locust -f "E:/JADS/Thesis/Experiment/Benchmark/browse_categories3.py" --csv="E:/JADS/Thesis/Experiment/Data Source/Data_1/variant"' + str(
            variant_03) + '"_"' + str(val) + ' --no-web -c ' + str(val) + ' -r 100 --run-time 30m')
