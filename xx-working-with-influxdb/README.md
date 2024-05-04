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

# Influx 2

Run the Smart Home simulator to send data to MQTT.

```
docker exec -ti mqttx-cli mqttx simulate -sc smart_home -c 100 conn  -h 'mosquitto-1' -p 1883
```




```
# Configuration for telegraf agent
[agent]
  ## Default data collection interval for all inputs
  interval = "10s"
  ## Rounds collection interval to 'interval'
  ## ie, if interval="10s" then always collect on :00, :10, :20, etc.
  round_interval = true

  ## Telegraf will send metrics to outputs in batches of at most
  ## metric_batch_size metrics.
  ## This controls the size of writes that Telegraf sends to output plugins.
  metric_batch_size = 1000

  ## Maximum number of unwritten metrics per output.  Increasing this value
  ## allows for longer periods of output downtime without dropping metrics at the
  ## cost of higher maximum memory usage.
  metric_buffer_limit = 10000

  ## Collection jitter is used to jitter the collection by a random amount.
  ## Each plugin will sleep for a random time within jitter before collecting.
  ## This can be used to avoid many plugins querying things like sysfs at the
  ## same time, which can have a measurable effect on the system.
  collection_jitter = "0s"

  ## Default flushing interval for all outputs. Maximum flush_interval will be
  ## flush_interval + flush_jitter
  flush_interval = "10s"
  ## Jitter the flush interval by a random amount. This is primarily to avoid
  ## large write spikes for users running a large number of telegraf instances.
  ## ie, a jitter of 5s and interval 10s means flushes will happen every 10-15s
  flush_jitter = "0s"

  ## By default or when set to "0s", precision will be set to the same
  ## timestamp order as the collection interval, with the maximum being 1s.
  ##   ie, when interval = "10s", precision will be "1s"
  ##       when interval = "250ms", precision will be "1ms"
  ## Precision will NOT be used for service inputs. It is up to each individual
  ## service input to set the timestamp at the appropriate precision.
  ## Valid time units are "ns", "us" (or "µs"), "ms", "s".
  precision = ""

  ## Log at debug level.
  # debug = false
  ## Log only error level messages.
  # quiet = false

  ## Log target controls the destination for logs and can be one of "file",
  ## "stderr" or, on Windows, "eventlog".  When set to "file", the output file
  ## is determined by the "logfile" setting.
  # logtarget = "file"

  ## Name of the file to be logged to when using the "file" logtarget.  If set to
  ## the empty string then logs are written to stderr.
  # logfile = ""

  ## The logfile will be rotated after the time interval specified.  When set
  ## to 0 no time based rotation is performed.  Logs are rotated only when
  ## written to, if there is no log activity rotation may be delayed.
  # logfile_rotation_interval = "0d"

  ## The logfile will be rotated when it becomes larger than the specified
  ## size.  When set to 0 no size based rotation is performed.
  # logfile_rotation_max_size = "0MB"

  ## Maximum number of rotated archives to keep, any older logs are deleted.
  ## If set to -1, no archives are removed.
  # logfile_rotation_max_archives = 5

  ## Pick a timezone to use when logging or type 'local' for local time.
  ## Example: America/Chicago
  # log_with_timezone = ""

  ## Override default hostname, if empty use os.Hostname()
  hostname = ""
  ## If set to true, do no set the "host" tag in the telegraf agent.
  omit_hostname = false

[[outputs.influxdb_v2]]
  ## The URLs of the InfluxDB cluster nodes.
  ##
  ## Multiple URLs can be specified for a single cluster, only ONE of the
  ## urls will be written to each interval.
  ##   ex: urls = ["https://us-west-2-1.aws.cloud2.influxdata.com"]
  urls = ["http://influxdb2:8086"]

  ## Token for authentication.
  token = "$INFLUX_TOKEN"

  ## Organization is the name of the organization you wish to write to; must exist.
  organization = "demo"

  ## Destination bucket to write into.
  bucket = "demo-bucket"

  ## The value of this tag will be used to determine the bucket.  If this
  ## tag is not set the 'bucket' option is used as the default.
  # bucket_tag = ""

  ## If true, the bucket tag will not be added to the metric.
  # exclude_bucket_tag = false

  ## Timeout for HTTP messages.
  # timeout = "5s"

  ## Additional HTTP headers
  # http_headers = {"X-Special-Header" = "Special-Value"}

  ## HTTP Proxy override, if unset values the standard proxy environment
  ## variables are consulted to determine which proxy, if any, should be used.
  # http_proxy = "http://corporate.proxy:3128"

  ## HTTP User-Agent
  # user_agent = "telegraf"

  ## Content-Encoding for write request body, can be set to "gzip" to
  ## compress body or "identity" to apply no encoding.
  # content_encoding = "gzip"

  ## Enable or disable uint support for writing uints influxdb 2.0.
  # influx_uint_support = false

  ## Optional TLS Config for use on HTTP connections.
  # tls_ca = "/etc/telegraf/ca.pem"
  # tls_cert = "/etc/telegraf/cert.pem"
  # tls_key = "/etc/telegraf/key.pem"
  ## Use TLS but skip chain & host verification
  # insecure_skip_verify = false

# Read metrics from MQTT topic(s)
[[inputs.mqtt_consumer]]
  ## Broker URLs for the MQTT server or cluster.  To connect to multiple
  ## clusters or standalone servers, use a separate plugin instance.
  ##   example: servers = ["tcp://localhost:1883"]
  ##            servers = ["ssl://localhost:1883"]
  ##            servers = ["ws://localhost:1883"]
  servers = ["tcp://mosquitto-1:1883"]

  ## Topics that will be subscribed to.
  topics = [
    "mqttx/simulate/smart_home/#",
  ]

  ## The message topic will be stored in a tag specified by this value.  If set
  ## to the empty string no topic tag will be created.
  topic_tag = ""

  ## Username and password to connect MQTT server.
  # username = "telegraf"
  # password = "metricsmetricsmetricsmetrics"

  ## Data format to consume.
  ## Each data format has its own unique set of configuration options, read
  ## more about them here:
  ## https://github.com/influxdata/telegraf/blob/master/docs/DATA_FORMATS_INPUT.md
  data_format = "json_v2"

  [[inputs.mqtt_consumer.json_v2]]
      measurement_name = "smart_home" # A string that will become the new measurement name
      timestamp_path = "timestamp" # A string with valid GJSON path syntax to a valid timestamp (single value)
      timestamp_format = "unix_ms" # A string with a valid timestamp format (see below for possible values)

    [[inputs.mqtt_consumer.json_v2.tag]]
      path = "home_id" # A string with valid GJSON path syntax
      rename = "id" # A string with a new name for the tag key
      type = "string" # A string specifying the type (int,uint,float,string,bool)
      optional = false # true: suppress errors if configured path does not exist

    [[inputs.mqtt_consumer.json_v2.tag]]
      path = "owner_name" # A string with valid GJSON path syntax
      rename = "owner" # A string with a new name for the tag key
      type = "string" # A string specifying the type (int,uint,float,string,bool)
      optional = false # true: suppress errors if configured path does not exist

    [[inputs.mqtt_consumer.json_v2.object]]
      path = "rooms"
```





Login with `influx` and `abc123abc123!`


```
from(bucket: "demo-bucket")
  |> range(start: v.timeRangeStart, stop: v.timeRangeStop)
  |> filter(fn: (r) => r["_measurement"] == "smart_home")
  |> filter(fn: (r) => r["_field"] == "humidity" or r["_field"] == "temperature")
  |> filter(fn: (r) => r["host"] == "telegraf")
  |> filter(fn: (r) => r["id"] == "009ae6d1-896c-437c-a4d7-a60c350c96e1" or r["id"] == "00acb406-594b-41b1-b169-98686d2f6519" or r["id"] == "00cd38c3-d688-4905-be9c-cf720f7d4c2a")
  |> filter(fn: (r) => r["owner"] == "Austin Hauck" or r["owner"] == "Marion Kihn" or r["owner"] == "Nick Rippin")
  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)
  |> yield(name: "mean")
```


