# Working with InfluxDB 1.x

In this workshop we will learn how to use the InfluxDB NoSQL database.

We assume that the platform described [here](../01-environment) is running and accessible. 

For this workshop we will be using an IoT device/sensor simulator and synthetic data generator. The generator is available as a [Docker image](https://hub.docker.com/repository/docker/trivadis/iot-simulator) as well as a [GitHub project](https://github.com/gschmutz/IotSimulator), which is a fork of [rradev/iosynth](https://github.com/rradev/iosynth) and has been extended to support more output adapters, such as Kafka, File and AWS IoT Core.

## Running the IoT Simulator

Before we can run the Simulator, we have to configure the kind of data we want it to generate.

We can do that using a JSON configuration file. Create a new folder `influxdb-workspace` in the home directory.

```
cd
mkdir influxdb-workspace
cd influxdb-workspace
```

Create the `devices-def.json` file

```
nano conf/devices-def.json
```

Add the following configuration. The topic name has no 

```
[
    {
        "type":"simple",
        "uuid":"",
        "topic":"sensor-reading",
        "partition":"{$uuid}",
        "sampling":{"type":"fixed", "interval":1000},
        "copy":10,
        "sensors":[
            {"type":"string", "name":"sensorType", "cycle":["temperature"]},
            {"type":"dev.timestamp",    "name":"ts", "format":"yyyy-MM-dd'T'HH:mm:ss.SSSZ"},
            {"type":"dev.uuid",         "name":"uuid"},
            {"type":"double_walk",   "name":"temp",  "min":10, "max":20},
            {"type":"double_walk",  "name":"level", "values": [1.1,3.2,8.3,9.4]},
            {"type":"string",        "name":"category", "random": ["a","b","c","d","e","f","g","h","i","j","k","l","m","n","o"]}
        ]
    }
]
```

Create a `logs` folder where the sensor will write its measurements to.

```
mkdir logs
```


Now let's run the IoT Simulator with this definition file and

```
docker run -v $PWD/devices-def.json:/conf/devices-def.json -v $PWD/logs:/logs trivadis/iot-simulator -t file -of /logs/iot-file.log -df /conf/devices-def.json
```

Alternatively you can also use the `-cl` flag to link to the config via URL (i.e. stored in GitHub)

```
docker run -v $PWD/logs:/logs trivadis/iot-simulator -t file -of /logs/iot-file.log -df https://raw.githubusercontent.com/gschmutz/IotSimulator/master/config/sensor-reading-sample.json
```

The data is published to a file in the `logs` folder.  

## Using Telegraf to send data to InfluxDB

Telegraf is part of the stack and we are going to showcase it here. But it can also be installed locally according to the [documentation](https://docs.influxdata.com/telegraf/v1.13/introduction/installation/).

### Create a Telegraf Configuration File

Inside the `influxdb-workspace` folder generate a Telegraf configuration file for using the `tail` input plugin and the `influxdb` output plugin. 

First let's create a template for the `telegraf.conf` config file. This can be done using the `telegraf` cli specifying the `--input-filter` and `--output-filter`.

First create a workspace

```
cd influxdb-workspace
```

and use the telegraf utiltity from within the running `telegraf` container: 

```
docker run telegraf telegraf --input-filter tail --output-filter influxdb config > telegraf.conf
```

Open the `telegraf.conf` in an editor of your choice and change the `outputs.influxdb` section to specify the url of the InfluxDB engine and the database to store the device metrics: 

```
[[outputs.influxdb]]
  ## The full HTTP or UDP URL for your InfluxDB instance.
  ##
  ## Multiple URLs can be specified for a single cluster, only ONE of the
  ## urls will be written to each interval.
  # urls = ["unix:///var/run/influxdb.sock"]
  # urls = ["udp://127.0.0.1:8089"]
  urls = ["http://influxdb:8086"]

  ## The target database for metrics; will be created as needed.
  ## For UDP url endpoint database needs to be configured on server side.
  database = "iot"
```

After that also change the `inputs.tail` section as shown below

```
[[inputs.tail]]
  ## files to tail.
  ## These accept standard unix glob matching rules, but with the addition of
  ## ** as a "super asterisk". ie:
  ##   "/var/log/**.log"  -> recursively find all .log files in /var/log
  ##   "/var/log/*/*.log" -> find all .log files with a parent dir in /var/log
  ##   "/var/log/apache.log" -> just tail the apache log file
  ##
  ## See https://github.com/gobwas/glob for more examples
  ##
  files = ["/logs/**.log.*"]
  
  ## Read file from beginning.
  from_beginning = true
  ## Whether file is a named pipe
  pipe = false

  ## Method used to watch for file updates.  Can be either "inotify" or "poll".
  # watch_method = "inotify"

  ## Data format to consume.
  ## Each data format has its own unique set of configuration options, read
  ## more about them here:
  ## https://github.com/influxdata/telegraf/blob/master/docs/DATA_FORMATS_INPUT.md
  data_format = "json"
  tag_keys = ["uuid","category"]

  json_string_fields = [ "temp", "level" ]

  json_name_key = "sensorType"
```

Now run a new container with the `telegraf.conf` config file configured above. We will start a new docker container, independent of the one running in the stack and create a volume mapping for a) the `telegraf.conf` config file and b) the folder to which the IoT simulator writes its log files (`logs`). 

```
docker run --rm -ti --name telegraf-agent --network docker_default -v ${PWD}/telegraf.conf:/telegraf.conf -v ${PWD}/logs:/logs telegraf telegraf --config /telegraf.conf
```

## Visualise InfluxDB data using Chronograf

One tool for visualisation is Chronograf, which is part of the so called [TICK Stack](https://www.influxdata.com/time-series-platform/) (TICK for **T**elegraf, **I**nfluxDB, **C**hronograf and **K**apacitor). 

In a browser window, navigate to <http://dataplatform:28152/> to open the Chronograf homepage. 

## Using Rest API to retrieve data

```
curl -X POST -H 'Accept: application/csv' -H 'Content-Type: application/vnd.flux' -i http://dataplatform:8086/api/v2/query --data 'from(bucket:"iot")
   |> range(start:-5m)
    '
```





