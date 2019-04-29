# NoSQL Platform on Docker

## Provision
The environment for this course is completly based on docker containers. 

In order to simplify the provisioning, a single docker-compose configuration is used. All the necessary software will be provisioned using Docker. 

You have the follwing options to start the environment:

 * **Local Virtual Machine** - a Virtual Machine with Docker and Docker Compose pre-installed will be distributed at by the course instructure. You will need 50 GB free disk space.
 * **Local Docker Installation** - you have a local Docker and Docker Compose setup in place which you want to use
 * **AWS Lightsail** - AWS Lightsail is a service in Amazon Web Services (AWS) with which we can easily startup a environment and provide all the necessary bootstraping as a script.

### Local Virtual Machine

Copy the Virtual Machine files to your local machine and start it up. 

```
export PUBLIC_IP=$(curl ipinfo.io/ip)
export DOCKER_HOST_IP=$(ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)
git clone https://github.com/gschmutz/nosql-workshop.git
cd ksql-workshop/01-environment/docker

# Startup Environment
docker-compose up -d
```


### Local Docker Installation



### AWS Lightsail
To start the whole stack on AWS Lightsail, use the following script

```
# Install Docker and Docker Compose
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable edge"
apt-get install -y docker-ce
sudo usermod -aG docker ubuntu

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Prepare Environment Variables
export PUBLIC_IP=$(curl ipinfo.io/ip)
export DOCKER_HOST_IP=$(ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)

# Get the project
cd /home/ubuntu 
git clone https://github.com/gschmutz/nosql-workshop.git
chown -R ubuntu:ubuntu nosql-workshop
cd nosql-workshop/01-environment/docker

# Startup Environment
sudo -E docker-compose up -d
```


```
tail -f /var/log/cloud-init-output.log
```

## Post Installation


### Add entry to local /etc/hosts File
To simplify working with the Streaming Platform, add the following entry to your local `/etc/hosts` file. 

```
40.91.195.92	nosqlplatform
```

## Services accessible on Streaming Platform
The following service are available as part of the platform:

Product | Type | Service | Url
------|------| --------| ----
Redis | Development | Redis Commander | <http://nosqlplatform:38083>
Cassandra | Development | Cassandra-Web | <http://nosqlplatform:3000>
MongoDB | Management | Admin Mongo | <http://nosqlplatform:1234>
MongoDB | Development | Mongo-Express | <http://nosqlplatform:38082>
Elasticsearch | Development | Dejavu | <http://nosqlplatform:1358>
Elasticsearch | Development | cerebro | <http://nosqlplatform:39000>
Elasticsearch | Development | Kibana | <http://nosqlplatform:5601>
Solr | Management | Solr | <http://nosqlplatform:8983>
Neo4J | Development | Neo4J | <http://nosqlplatform:7474>
Grafana | Development | Grafana | <http://nosqlplatform:3010>
Zepplin | Development | Zeppelin | <http://nosqlplatform:38081>
Jupyter | Development | Jupyter | <http://nosqlplatform:10000>


## Deprovisiongin the environment
To stop the environment, execute the following command:

```
docker-compose stop
```

after that it can be re-started using `docker-compose start`.

To stop and remove all running container, execute the following command:

```
docker-compose down
```

