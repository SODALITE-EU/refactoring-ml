LIMBO: https://github.com/joakimkistowski/HTTP-Load-Generator 

1. Change IP in teastore_browse to ingress gateway IP of service mesh.
2. On one machine (i.e., VM), load generator machine needs to be ran (running the java program):

java -jar httploadgenerator.jar loadgenerator

3. On another machine run the load director:

java -jar httploadgenerator.jar director -s 34.142.76.38 -a intensity.csv -l teastore_browse.lua -o results.csv -t 50


Load should be generated now.