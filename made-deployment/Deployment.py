# Packages are imported. Unusual packages are installed.

import os

os.system('pip3 install pyyaml')
os.system('pip3 install ruamel.yaml')

import yaml
import ruamel.yaml
import copy
from ruamel.yaml.scalarstring import (DoubleQuotedScalarString as dq, 
                                      SingleQuotedScalarString as sq)

# Create GCP firewall rules for ingress and egress directions
os.system('gcloud compute firewall-rules create ingressrule --allow tcp:30000-33000,tcp:8000-11000,tcp:443,tcp:80,tcp:24226 --direction INGRESS')
os.system('gcloud compute firewall-rules create egressrule --allow tcp:30000-33000,tcp:8000-11000,tcp:443,tcp:80 --direction EGRESS')

# table of hostnames and external ip's is requested
nodeinfo_list = os.popen("gcloud compute instances list --format='table(NAME,EXTERNAL_IP)'").read().split()


# list of dictionaries for each gke cluster node is made
nodeinfo_list = [{'name' : nodeinfo_list[i],'ip' : nodeinfo_list[i+1]} for i in range(0, len(nodeinfo_list), 1) if 'gke' in nodeinfo_list[i]]

# label nodes 1 to 6
for id in range(1, len(nodeinfo_list)+1):
    os.system('kubectl label nodes ' + nodeinfo_list[id-1]['name'] + ' nodeid=' + str(id))

# Get working directory
directory = os.getcwd()

# Download Istio if necessary
if os.path.isdir((os.getcwd()[:-4]) + 'istio-1.9.2') == False:
    os.system('curl -L https://istio.io/downloadIstio | ISTIO_VERSION=1.9.2 TARGET_ARCH=x86_64 sh -')
    os.system('cd istio-1.9.2')
    os.system('export PATH=$PWD/bin:$PATH')
    os.system('cd')

# install istio
os.system('istioctl install --set profile=demo -y')

# annotate default namespace for sidecar injection
os.system('kubectl label namespace default istio-injection=enabled')

# deploy teastore microservices and label them as teastore
os.system('kubectl apply -f Infrastructure/Teastore/teastore-clusterip.yaml')
os.system('kubectl label deploy --all app=teastore -n default')

# create ingressgateway for service mesh
os.system('kubectl apply -f Infrastructure/Istio/istioGateway.yml')

#  install litmus operator
os.system('kubectl apply -f https://litmuschaos.github.io/litmus/litmus-operator-v1.13.6.yaml')

# install litmus experiments
os.system('kubectl apply -f https://hub.litmuschaos.io/api/chaos/1.13.7?file=charts/generic/experiments.yaml -n default')

# deploy litmus admin service account
os.system('kubectl apply -f Infrastructure/Litmus/adminrbac.yml -n default')

# deploy litmus chaos exporter
os.system('kubectl apply -f Infrastructure/Litmus/chaos-exporter.yml -n litmus')

# create monitoring namespace
os.system('kubectl create ns monitoring')

# deploy skydive analyzer and agents
os.system('kubectl apply -f Infrastructure/Skydive/skydive_agents_analyzer.yml')

# ip change in prom_skydive_con.yml to existing k8s cluster node ip (in 2 places)
file_path = 'Infrastructure/Skydive/prom_skydive_con.yml'
with open(file_path, 'r') as f:
    deployment_dict = yaml.safe_load_all(f)
    deployment_dict = list(deployment_dict)
    deployment_dict[0]['spec']['template']['spec']['containers'][0]['env'][0]['value'] = str(nodeinfo_list[0]['ip']) + ":32505"
    deployment_dict[0]['spec']['template']['spec']['containers'][0]['env'][2]['value'] = "ws://" + str(nodeinfo_list[0]['ip']) + ":32505/ws/subscriber/flow"

with open(file_path, 'w') as f:
    yaml.safe_dump_all(deployment_dict, f, default_flow_style=False)

# deploy prometheus_skydive_connector
os.system('kubectl apply -f  Infrastructure/Skydive/prom_skydive_con.yml -n monitoring')

# run skydive gremlin query !!!TODO
#os.system('sudo docker run --net=host -e SKYDIVE_ANALYZERS=35.246.242.101:32505 skydive/skydive client capture create --type pcap --gremlin "G.V().Has(\'Type\', \'device\', \'Name\', \'eth0\')"')

# deploy kube-state-metric
os.system('kubectl apply -f Infrastructure/Kubernetes/kube-state-metrics-configs/')

# change ip in prometheus config file

#change ip prom config for skydive connector and litmus export to right values.
testyaml = ruamel.yaml.YAML()
with open('Infrastructure/Prometheus/kubernetes-prometheus/config-map.yaml', 'r') as read_file:
    prom_config = testyaml.load_all(read_file)
    
    prom_config = list(prom_config)
    #print(prom_config[0]['data']['prometheus.yml'])    
    prom_config[0]['data']['prometheus.yml'] = prom_config[0]['data']['prometheus.yml'].replace('skydive_ip_port', str(nodeinfo_list[0]['ip']) + ':32222')
    prom_config[0]['data']['prometheus.yml'] = prom_config[0]['data']['prometheus.yml'].replace('litmus_ip_port', str(nodeinfo_list[3]['ip']) + ':31111')


with open('Infrastructure/Prometheus/kubernetes-prometheus/config-map.yaml', 'w') as created_file:
    testyaml.default_flow_style = False
    testyaml.dump(prom_config[0], created_file)

# deploy prometheus
os.system('kubectl apply -f Infrastructure/Prometheus/kubernetes-prometheus/ -n monitoring')

# deploy prometheus node exporters
os.system('kubectl apply -f Infrastructure/Prometheus/kubernetes-node-exporter/ -n monitoring')

# Function for defining unique parameters
# define the experiment parameters based on specific experiments
def define_exp_parameters(experiment, exp_doc, node_list):
    
    deployment_list = []
    # Node cpu hog parameters
    if ('cpu' in experiment) and ('node' in experiment):
                
        for node in node_list:
            for duration in range(50, 81, 1):
                for cpu_cores in range(2, 5):
                    
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(duration)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(cpu_cores)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] =  sq(node)
                    deployment_list.append(copy.deepcopy(exp_doc))
                              
    #  Pod cpu hog parameters
    elif ('cpu' in experiment) and ('pod' in experiment):
        
        for duration in range(50, 81, 1):
            for cpu_cores in range(2, 5):
                
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(cpu_cores)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(duration)
                deployment_list.append(copy.deepcopy(exp_doc))

    # Node memory hog parameters
    elif ('mem' in experiment) and ('node' in experiment):

        for node in node_list:
            for duration in range(50, 81, 1):
                for ram_perc in range(20, 41):
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(duration)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(ram_perc)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = sq(node)
                    deployment_list.append(copy.deepcopy(exp_doc))

    # Pod memory hog parameters
    elif ('mem' in experiment) and ('pod' in experiment):
        
        for duration in range(50, 81, 1):
            for ram in range(1500, 3000, 50):
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(ram)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(duration)
                
                deployment_list.append(copy.deepcopy(exp_doc))

    # Node io stress parameters
    elif ('iostress' in experiment) and ('node' in experiment):
        
        for node in node_list:
            for duration in range(50, 81, 1):
                for fsu_perc in range(20, 41):
                    for nr_workers in range(3, 6):
                        for nr_cpu in [1, 2]:
                            exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(duration)
                            exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(fsu_perc)
                            exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = str(nr_cpu)
                            exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'] = str(nr_workers)
                            exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'] = sq(node)
                            deployment_list.append(copy.deepcopy(exp_doc))
    
    # Pod io stress parameters
    elif ('iostress' in experiment) and ('pod' in experiment):
        
        for duration in range(50, 81, 1):
            for fsu_perc in range(20, 41):
                for nr_workers in range(3, 6):
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(duration)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(fsu_perc)
                    exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = str(nr_workers)
                    deployment_list.append(copy.deepcopy(exp_doc))

    # Pod delete parameters
    elif ('del' in experiment):
        
        for duration in range(12, 20, 1):
            for interval in range(3, 7):
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = str(interval)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(duration)
                
                deployment_list.append(copy.deepcopy(exp_doc))
    
    # Network latency parameters
    elif ('netlat' in experiment):
        
        for duration in range(50, 81, 1):
            for latency in range(1600, 2800, 50):
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(latency)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = str(duration)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'])
                
                deployment_list.append(copy.deepcopy(exp_doc))
    
    # Network loss parameters
    elif 'netloss' in experiment:
        
        for duration in range(50, 81, 1):
            for packet_loss_perc in range(80, 101):
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(packet_loss_perc)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = str(duration)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'])
                deployment_list.append(copy.deepcopy(exp_doc))

    # Network corruption parameters
    elif 'netcorr' in experiment:
        for duration in range(50, 81, 1):
            for packet_corr_perc in range(80, 101):
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][0]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][1]['value'] = str(duration)
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][2]['value'] = str(packet_corr_perc)
                
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][3]['value'])
                exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'] = sq(exp_doc[0]['spec']['experiments'][0]['spec']['components']['env'][4]['value'])
                deployment_list.append(copy.deepcopy(exp_doc))
    
    return deployment_list


# Loading template files, generating different parameters, and dumping them as seperate YAML deployment files.
#node related
template_dir = 'Experiments/templates'
base_dir = 'Experiments/'
experiment_list = os.listdir(template_dir)
node_list = [node['name'] for node in nodeinfo_list]
testyaml = ruamel.yaml.YAML() 
for experiment in [experiment for experiment in experiment_list if 'disk' not in experiment]:
    
    with open(template_dir + "/" + experiment, 'r') as read_file:
        deployments = testyaml.load_all(read_file)
        deployments = list(deployments)
        deployments = define_exp_parameters(experiment, deployments, node_list)
        
       
    i = 1
    for deployment in deployments:
        with open(base_dir + experiment.split('.')[0] + "/" + experiment.split('.')[0] + "_" + str(i) + ".yml", 'w') as created_file:
            
            testyaml.default_flow_style = False
            
            testyaml.dump(deployment[0], created_file)
            
        i += 1