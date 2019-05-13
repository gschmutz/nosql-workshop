// Either create the graph in studio and add the following in a cell
//  or uncomment and run the following with the gremlin console
/*
:remote config alias reset
system.graph("killrvideo").drop()
system.graph("killrvideo").ifNotExists().create()
:remote config alias g killrvideo.g
:remote config timeout max
*/

// Define properties 
schema.propertyKey("genreId").Text().create();
schema.propertyKey("personId").Text().create();
schema.propertyKey("userId").Text().create();
schema.propertyKey("movieId").Text().create();
schema.propertyKey("name").Text().create();
schema.propertyKey("age").Int().create();
schema.propertyKey("gender").Text().create();
schema.propertyKey("title").Text().create();
schema.propertyKey("year").Int().create();
schema.propertyKey("duration").Int().create();
schema.propertyKey("country").Text().create();
schema.propertyKey("production").Text().multiple().create();
schema.propertyKey("rating").Int().create();

// Define vertex labels
schema.vertexLabel("genre").properties("genreId","name").create();
schema.vertexLabel("person").properties("personId","name").create();
schema.vertexLabel("user").properties("userId","age","gender").create();
schema.vertexLabel("movie").properties("movieId","title","year","duration","country","production").create();

// Define edge labels
schema.edgeLabel("knows").connection("user","user").create();
schema.edgeLabel("rated").single().properties("rating").connection("user","movie").create();
schema.edgeLabel("belongsTo").single().connection("movie","genre").create();
schema.edgeLabel("actor").connection("movie","person").create();              // multiple() due to data
schema.edgeLabel("director").single().connection("movie","person").create();
schema.edgeLabel("composer").single().connection("movie","person").create();
schema.edgeLabel("screenwriter").connection("movie","person").create();       // multiple() due to data
schema.edgeLabel("cinematographer").single().connection("movie","person").create();

// Define vertex indexes
schema.vertexLabel("genre").index("genresById").materialized().by("genreId").add();
schema.vertexLabel("genre").index("genresByName").materialized().by("name").add();
schema.vertexLabel("person").index("personsById").materialized().by("personId").add();
schema.vertexLabel("person").index("personsByName").materialized().by("name").add();
schema.vertexLabel("user").index("usersById").materialized().by("userId").add();
schema.vertexLabel("user").index("usersByAge").secondary().by("age").add();
schema.vertexLabel("movie").index("moviesById").materialized().by("movieId").add();
schema.vertexLabel("movie").index("moviesByTitle").materialized().by("title").add();
schema.vertexLabel("movie").index("moviesByYear").secondary().by("year").add();

// Define edge indexes
schema.vertexLabel("user").index("toMoviesByRating").outE("rated").by("rating").add();
schema.vertexLabel("movie").index("toUsersByRating").inE("rated").by("rating").add();
