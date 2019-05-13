config load_new: true, create_schema: false

// If the user specifies an inputpath on the command-line, use that.
// Otherwise check the data directory from the data directory from where the loader is run.
if (inputpath == '')
    path = new java.io.File('.').getCanonicalPath() + '/data/'
else
    path = inputpath + '/'

def genres           = File.text(path + "genres.dat").delimiter("::")
                           .header("genreId", "name")
def persons          = File.text(path + "persons.dat").delimiter("::")
                           .header("personId", "name")
def users            = File.text(path + "users.dat").delimiter("::")
                           .header("userId", "gender", "age")
def movies           = File.text(path + "movies.dat").delimiter("::")
                           .header("movieId","title","year","duration","country","rating","votes",
                           "genres","actors","directors","composers","screenwriters","cinematographers","production")
def friendship       = File.text(path + "friendship.dat").delimiter("::")
                           .header("userId1", "userId2")
def ratings          = File.text(path + "ratings.dat").delimiter("::")
                           .header("userId", "movieId","rating")
    genres           = genres.map     { it["genreId"]    = "g" + it["genreId"];  it }
    persons          = persons.map    { it["personId"]   = "p" + it["personId"]; it }
    users            = users.map      { it["userId"]     = "u" + it["userId"];   it }
    movies           = movies.map     { it["movieId"]    = "m" + it["movieId"];  
                                        it["production"] = it["production"].toString().split("\\|"); it }
def belongsTo        = movies.flatMap { def movieId = it["movieId"];
                                        it["genres"].split("\\|")
                                                    .collect{ ["movieId": movieId, "genre": it] } }         
def actors           = movies.flatMap { def movieId = it["movieId"];
                                        it["actors"].split("\\|")
                                                    .collect{ ["movieId": movieId, "actor": it] } }   
                             .filter  { !it["actor"].isEmpty() }
def directors        = movies.flatMap { def movieId = it["movieId"];
                                        it["directors"].split("\\|")
                                                       .collect{ ["movieId": movieId, "director": it] } }    
                             .filter  { !it["director"].isEmpty() }
def composers        = movies.flatMap { def movieId = it["movieId"];
                                        it["composers"].split("\\|")
                                                       .collect{ ["movieId": movieId, "composer": it] } }    
                             .filter  { !it["composer"].isEmpty() }
def screenwriters    = movies.flatMap { def movieId = it["movieId"];
                                        it["screenwriters"].split("\\|")
                                                           .collect{ ["movieId": movieId, "screenwriter": it] } }    
                             .filter  { !it["screenwriter"].isEmpty() }
def cinematographers = movies.flatMap { def movieId = it["movieId"];
                                        it["cinematographers"].split("\\|")
                                                              .collect{ ["movieId": movieId, "cinematographer": it] } }             
                             .filter  { !it["cinematographer"].isEmpty() }                                                                                                                                                                                                                                                                                                                    
    friendship       = friendship.map { it["userId1"]    = "u" + it["userId1"]; 
                                        it["userId2"]    = "u" + it["userId2"]; it }
    ratings          = ratings.map    { it["userId"]     = "u" + it["userId"];  
                                        it["movieId"]    = "m" + it["movieId"]; it }

load(genres).asVertices {
    label "genre"
    key "name"
}
load(persons).asVertices {
    label "person"
    key "name"
}
load(users).asVertices {
    label "user"
    key "userId"
}
load(movies).asVertices {
    label "movie"
    key "movieId"
    ignore "rating"
    ignore "votes"
    ignore "genres"
    ignore "actors"
    ignore "directors"
    ignore "composers"
    ignore "screenwriters"
    ignore "cinematographers"            
}
load(belongsTo).asEdges {
    isNew()
    label "belongsTo"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "genre", {
        exists()
        label "genre"
        key "name"
    }
}
load(actors).asEdges {
    isNew()
    label "actor"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "actor", {
        exists()
        label "person"
        key "name"
    }
}
load(directors).asEdges {
    isNew()
    label "director"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "director", {
        exists()
        label "person"
        key "name"
    }
}
load(composers).asEdges {
    isNew()
    label "composer"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "composer", {
        exists()
        label "person"
        key "name"
    }
}
load(screenwriters).asEdges {
    isNew()
    label "screenwriter"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "screenwriter", {
        exists()
        label "person"
        key "name"
    }
}
load(cinematographers).asEdges {
    isNew()
    label "cinematographer"
    outV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
    inV "cinematographer", {
        exists()
        label "person"
        key "name"
    }
}
load(friendship).asEdges{
    isNew()
    label "knows"
    outV "userId1", { 
        exists()
        label "user"
        key "userId" 
    }
    inV  "userId2", { 
        exists()
        label "user"
        key "userId" 
    }
}
load(ratings).asEdges {
    isNew()
    label "rated"
    outV "userId", {
        exists()
        label "user"
        key "userId"
    }
    inV "movieId", {
        exists()
        label "movie"
        key "movieId"
    }
}
