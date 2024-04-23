# nosql-platform - List of Services

| Service | Links | External<br>Port | Internal<br>Port | Description
|--------------|------|------|------|------------
|[admin-mongo](./documentation/services/admin-mongo )|[Web UI](http://192.168.1.102:28124)|28124<br>|1234<br>|MongoDB Admin UI
|[adminer](./documentation/services/adminer )|[Web UI](http://192.168.1.102:28131)|28131<br>|8080<br>|Relational Database Admin UI
|[cassandra-1](./documentation/services/cassandra )||29042<br>7199<br>9160<br>|9042<br>7199<br>9160<br>|wide-column NoSQL database
|[cassandra-web](./documentation/services/cassandra-web )|[Web UI](http://192.168.1.102:28120)|28120<br>|3000<br>|Cassandra Web UI
|[cerebro](./documentation/services/cerbero )|[Web UI](http://192.168.1.102:28126)|28126<br>|9000<br>|UI for Elasticsearch
|[cloudbeaver](./documentation/services/cloudbeaver )|[Web UI](http://192.168.1.102:8978)|8978<br>|8978<br>|Cloud Database Manager
|[dejavu](./documentation/services/dejavu )|[Web UI](http://192.168.1.102:28125)|28125<br>|1358<br>|UI for Elasticsearch
|[elastichq](./documentation/services/elastichq )|[Web UI](http://192.168.1.102:28127)|28127<br>|5000<br>|UI for Elasticsearch
|[elasticsearch-1](./documentation/services/elasticsearch )|[Rest API](http://192.168.1.102:9200)|9200<br>9300<br>|9200<br>9300<br>|Search-engine NoSQL store
|[elasticvue](./documentation/services/elasticvue )|[Web UI](http://192.168.1.102:28275)|28275<br>|8080<br>|UI for Elasticsearch
|[graphdb-1](./documentation/services/graphdb )|[Web UI](http://192.168.1.102:17200)|17200<br>|7200<br>|Graph Database
|[influxdb2](./documentation/services/influxdb2 )|[Web UI](http://192.168.1.102:19999) - [Rest API](http://192.168.1.102:19999/api/v2)|19999<br>|8086<br>|Timeseries Database
|[jupyter](./documentation/services/jupyter )|[Web UI](http://192.168.1.102:28888)|28888<br>14040-14044<br>|8888<br>4040-4044<br>|Web-based interactive development environment for notebooks, code, and data
|[kibana](./documentation/services/kibana )|[Web UI](http://192.168.1.102:5601)|5601<br>|5601<br>|Visualization for Elasticsearch
|[markdown-viewer](./documentation/services/markdown-viewer )|[Web UI](http://192.168.1.102:80)|80<br>|3000<br>|Platys Platform homepage viewer
|[mongo-1](./documentation/services/mongodb )||27017<br>|27017<br>|Document NoSQL database
|[mongo-express](./documentation/services/mongo-express )|[Web UI](http://192.168.1.102:28123)|28123<br>|8081<br>|MongoDB UI
|[neo4j-1](./documentation/services/neo4j )|[Web UI](http://192.168.1.102:7474)|7474<br>7687<br>|7474<br>7687<br>|Native graph database management system
|[postgresql](./documentation/services/postgresql )||5432<br>|5432<br>|Open-Source object-relational database system
|[redis-1](./documentation/services/redis )||6379<br>|6379<br>|Key-value store
|[redis-commander](./documentation/services/redis-commander )|[Web UI](http://192.168.1.102:28119)|28119<br>|8081<br>|Graphical interface for Redis
|[redis-insight](./documentation/services/redis-insight )|[Web UI](http://192.168.1.102:28174)|28174<br>|5540<br>|Graphical interface for Redis
|[telegraf](./documentation/services/telegraf )||||Agent for collecting, processing, aggregating, and writing metrics
|[wetty](./documentation/services/wetty )|[Web UI](http://192.168.1.102:3001)|3001<br>|3000<br>|A terminal window in Web-Browser|

**Note:** init container ("init: true") are not shown