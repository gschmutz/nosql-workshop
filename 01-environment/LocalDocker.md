# Local Docker Environment

# Startup
But 

```
# Prepare Environment Variables
export PUBLIC_IP=$(curl ipinfo.io/ip)
export DOCKER_HOST_IP=$(ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)

# needed for elasticsearch
sudo sysctl -w vm.max_map_count=262144   

# Get the project
cd /home/ubuntu 
git clone https://github.com/gschmutz/nosql-workshop.git
chown -R ubuntu:ubuntu nosql-workshop
cd nosql-workshop/01-environment/docker

# Startup Environment
sudo -E docker-compose up -d
```
