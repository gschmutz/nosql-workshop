# NoSQL Platform in Docker
The environment for this course is completly based on docker containers. 

In order to simplify the provisioning, a single docker-compose configuration is used. All the necessary software will be provisioned using Docker. 

You have the follwing options to start the environment:

 * **Local Virtual Machine** - a Virtual Machine with Docker and Docker Compose pre-installed will be distributed at by the course instructure. You will need 50 GB free disk space.
 * **Local Docker Installation** - you have a local Docker and Docker Compose setup in place which you want to use
 * **AWS Lightsail** - AWS Lightsail is a service in Amazon Web Services (AWS) with which we can easily startup a environment and provide all the necessary bootstraping as a script.

## Local Virtual Machine

Copy the Virtual Machine files to your local machine and start it up. 

## Local Docker Installation



## AWS Lightsail
To start the whole stack on AWS Lightsail, use the following script

```
# Install Docker and Docker Compose
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable edge"
apt-get install -y docker-ce
sudo usermod -a -G docker $USER

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/1.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose

# Install wget
apt-get install -y wget

# Prepare Environment
export PUBLIC_IP=$(curl ipinfo.io/ip)
export DOCKER_HOST_IP=$(ip addr show eth0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)
git clone https://github.com/gschmutz/nosql-workshop.git
cd nosql-workshop/01-environment/docker

# Startup Environment
docker-compose up -d
```






### Add entry to local /etc/hosts File
To simplify working with the Streaming Platform, add the following entry to your local `/etc/hosts` file. 

```
40.91.195.92	streamingplatform
```

## Services accessible on Streaming Platform
The following service are available as part of the platform:

Type | Service | Url
------|------- | -------------
Development | StreamSets Data Collector | <http://streamingplatform:18630>
Development | Apache NiFi | <http://streamingplatform:28080/nifi>
Governance | Schema Registry UI  | <http://streamingplatform:8002>
Governance | Schema Registry Rest API  | <http://streamingplatform:8081>
Management | Kafka Connect UI | <http://streamingplatform:8003>
Management | Kafka Manager  | <http://streamingplatform:39000>
Management | Kafdrop  | <http://streamingplatform:9020>
Management | Zoonavigator  | <http://streamingplatform:8010>
Management | Confluent Control Center | <http://streamingplatform:9021>


## Stop the environment
To stop the environment, execute the following command:

```
docker-compose stop
```

after that it can be re-started using `docker-compose start`.

To stop and remove all running container, execute the following command:

```
docker-compose down
```

