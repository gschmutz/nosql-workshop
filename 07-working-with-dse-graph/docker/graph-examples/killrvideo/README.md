# KillrVideo

The KillrVideo dataset comes from the [DataStax Academy 330 course](https://academy.datastax.com/resources/ds330-datastax-enterprise-graph).  It includes metadata about films, users, and ratings.

Data files:

* movies.dat (920 records)
  movie_id::title::year::duration::country::rating::votes::genres::actors::directors::composers::screenwriters::cinematographers::production_companies
  Note: multiple genres, actor, directors, composers, screenwriters, cinematographers, and production_companies are separated by |
* users.dat (1100 records)
  user_id::gender::age
* ratings.dat (48094 records)
  user_id::movie_id::rating
  Note: only subset of user ratings is made available; if aggregation is performed, an average rating (but not the number of votes) will approximately match the rating in movies.dat
* friendship.dat (3500 records)
  user_id::friend_user_id
* genres.dat (18 records)
  genre_id::genre
* persons.dat (8759 records)
  person_id::name
  Note: these represent all unique actors, directors, composers, screenwriters, and cinematographers
  Note: some non-person exceptions include "Animation" for actors, "Miscellaneous" for composers, "The Beatles" for actors, etc. 

Interesting dataset properties:

* Users form 4 natural clusters based on movies they like
  Users with ids 1-200 (A), 201-700 (B), 701-900 (C), 901-1100 (D)
* Users that belong to the same cluster may exhibit age and/or gender similarities 
  A: most of age 12-17
  B: most of age 18-65
  C: most of age 20-40, more males
  D: most of age 18-35, more females
* Friendship or "knows" relationships form a small world with 6 degrees of separation
* Clustering coefficient for "knows" relationships is different for each user group
  (Largest) C > A and D > B (Smallest)
  
## Loading

The following steps assumes that there is a running DSE Graph instance in place. First, either load the script in studio or do as the schema script specifies and uncomment the specified lines before executing the script as follows:

```text
$ bin/dse gremlin-console -i ../graph-examples/killrvideo/schema.groovy
```

Be sure to properly set the path to the `schema.groovy` file. Now the data can be loaded with the [DSE Graph Loader](https://docs.datastax.com/en/dse/5.1/dse-dev/datastax_enterprise/graph/dgl/dglOverview.html):

```text
java -jar target/dse-graph-loader-*-uberjar.jar killrvideo/killrvideo-mapping.groovy -graph killrvideo -preparation false
```

Going back to the open Gremlin Console, it can be quickly validated that the data is loaded and indices are in place:

```text
gremlin> g.V().hasLabel('movie').has('title','Young Guns')
==>v[{~label=movie, community_id=823607168, member_id=475}]
```


