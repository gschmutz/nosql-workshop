# Working with Neo4J
In this workshop we will learn how to use the Neo4J NoSQL database.

We assume that the platform described [here](../01-environment/README.md) is running and accessible. 

In this workshop you learn how to use the Neo4J for querying, visualisation, and data interaction. [Neo4J Browser](https://neo4j.com/developer/guide-neo4j-browser/) is part of Neo4J and offers a browser-based interface for adding data, running queries, creating relationships, and more. It also provides an easy way to visualise the data in the database.

## Connecting with Neo4J Browser

In a browser window, navigate to <http://dataplatform:7474> and you should directly land on the Neo4j Browser login screen. 

![Alt Image Text](./images/neo4j-login.png "Neo4J Browser")

Enter `bolt://dataplatform:7687` into the **Connect URL**, `neo4j` into the **Username** and `abc123!` into the **Password** field and click **Connect**. 

If successfully connected, you should see a page similar to the one shown below:

![Alt Image Text](./images/neo4j-browser-home.png "Neo4J Browser")

Neo4J comes with some predefined tutorials, which provide an easy way for loading some data into the graph and then using that graph to exercise the query capabilities of the graph. 

On the top panel, enter `:play movie graph` to start the **Movie Graph** tutorial. 

![Alt Image Text](./images/neo4j-play-moviegraph.png "Neo4J Browser")

Execute the statement by either hitting <Enter> or click on the play arrow on the top right corner.

![Alt Image Text](./images/neo4j-moviegraph.png "Neo4J Browser")

## Loading the Movie Graph

Use the pin on the Movie Graph panel to pin that panel to the top. We will follow the instructions in this window step by step. Click on the next step in the navigation bar on the bottom of the panel to navigate to 2/8.

![Alt Image Text](./images/neo4j-create-graph.png "Neo4J Browser")

Click on the little arrow, left to the `CREATE ..` statement and it should show up in the top panel.

Execute the `CREATE ..` command to create the movie graph and add some sample movie data to play with.  

Before continuing with the next step, let's see how the graph looks like. Click on the database icon on the top left corner of the Neo4J browser.

![Alt Image Text](./images/neo4j-database-view.png "Neo4J Browser")

We can see the different **Node Labels**, the **Relationship Types** and **Property Keys** which have been created for the Movie Graph and how many have been created of each type. 

## Example Queries

Navigate to step 3/8 to find some Cipher statements for finding the information in the graph.

![Alt Image Text](./images/neo4j-find-queries.png "Neo4J Browser")

The first statements, finds the actor named "Tom Hanks"

```
MATCH (tom {name: "Tom Hanks"}) RETURN tom
```

Execute it and scroll down to see the result in a graphical way. 

![Alt Image Text](./images/neo4j-find-tom-hanks.png "Neo4J Browser")

We have only matched on a single Person, therefore only a single node is shown. 

Not let's find the movie with title "Cloud Atlas"...

```
MATCH (cloudAtlas {title: "Cloud Atlas"}) RETURN cloudAtlas
```

The result is similar to the one before, but this time another type of node, a **Movie** node is returned and that's why it is shown in another color. You can click on a node and expand the relationship from/to the **Cloud Atlas** movie node.

![Alt Image Text](./images/neo4j-show-expand-menu.png "Neo4J Browser")

Click on expand and you see all nodes related to the movie node

![Alt Image Text](./images/neo4j-show-related-nodes.png "Neo4J Browser")

We can see that these are all of type Person (shown by all having the same color). 

Continue with the other statements on the step 3/8 panel and then continue with the other panels. 

You will see many interesting queries, showing the power of a graph database, such as

Show Tom Hanks' co-actors:

```
MATCH (tom:Person {name:"Tom Hanks"})-[:ACTED_IN]->(m)<-[:ACTED_IN]-(coActors) RETURN coActors.name
```

or the Bacon path, the shortest path of any relationships to Meg Ryan

```
MATCH p=shortestPath(
(bacon:Person {name:"Kevin Bacon"})-[*]-(meg:Person {name:"Meg Ryan"})
)
RETURN p
```

the result will show the shortest path from Kevin Bacon to Meg Ryan

![Alt Image Text](./images/neo4j-shortest-path.png "Neo4J Browser")


