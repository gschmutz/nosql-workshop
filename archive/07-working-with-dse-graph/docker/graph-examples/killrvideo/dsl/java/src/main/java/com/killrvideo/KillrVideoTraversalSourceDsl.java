package com.killrvideo;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.killrvideo.KV.KEY_COUNTRY;
import static com.killrvideo.KV.KEY_DURATION;
import static com.killrvideo.KV.KEY_MOVIE_ID;
import static com.killrvideo.KV.KEY_PRODUCTION;
import static com.killrvideo.KV.KEY_TITLE;
import static com.killrvideo.KV.KEY_USER_ID;
import static com.killrvideo.KV.KEY_YEAR;
import static com.killrvideo.KV.VERTEX_MOVIE;
import static com.killrvideo.KV.VERTEX_USER;

/**
 * The KillrVideo DSL definition that produces the custom {@code GraphTraversalSource}.
 * <p/>
 * The DSL definition must be a class that extends {@code GraphTraversalSource} and should be referenced in the
 * {@code GremlinDsl} annotation on the {@code GraphTraversal} extension - in this example
 * {@link KillrVideoTraversalDsl}. The methods on this class will be exposed with the other traversal start steps
 * on {@code GraphTraversalSource}.
 */
public class KillrVideoTraversalSourceDsl extends GraphTraversalSource {

    public KillrVideoTraversalSourceDsl(final Graph graph, final TraversalStrategies traversalStrategies) {
        super(graph, traversalStrategies);
    }

    public KillrVideoTraversalSourceDsl(final Graph graph) {
        super(graph);
    }

    /**
     * Gets movies by their title.
     */
    public GraphTraversal<Vertex, Vertex> movies(String title, String... additionalTitles) {
        List<String> titles = Stream.concat(Stream.of(title), Stream.of(additionalTitles)).collect(Collectors.toList());
        GraphTraversal traversal = this.clone().V();
        traversal = traversal.hasLabel(VERTEX_MOVIE);
        if (titles.size() == 1)
            traversal = traversal.has(KEY_TITLE, titles.get(0));
        else if (titles.size() > 1)
            traversal = traversal.has(KEY_TITLE, P.within(titles));

        return traversal;
    }

    /**
     * Gets users by their identifier.
     */
    public GraphTraversal<Vertex, Vertex> users(String userId, String... additionalUserIds) {
        List<String> userIds = Stream.concat(Stream.of(userId), Stream.of(additionalUserIds)).collect(Collectors.toList());
        GraphTraversal traversal = this.clone().V();
        traversal = traversal.hasLabel(VERTEX_USER);
        if (userIds.size() == 1)
            traversal = traversal.has(KEY_USER_ID, userIds.get(0));
        else if (userIds.size() > 1)
            traversal = traversal.has(KEY_USER_ID, P.within(userIds));

        return traversal;
    }

    /**
     * An overloaded step for {@link #movie(String, String, String, String, int, int)} where the "country" and
     * "production" parameters are passed in as {@code null}.
     */
    public GraphTraversal<Vertex, Vertex> movie(String movieId, String title, int year, int duration) {
        return movie(movieId, title, null, null, year, duration);
    }

    /**
     * Ensures that a "movie" exists.
     * <p/>
     * This step performs a number of validations on the various parameters passed to it and then checks for existence
     * of the movie based on the identifier for the movie. If it exists then the movie is returned with its mutable
     * properties updated (all are mutable except for the "movieId" as defined by this DSL). If it does not exist then
     * a new "movie" vertex is added.
     */
    public GraphTraversal<Vertex, Vertex> movie(String movieId, String title, String country, String production, int year, int duration) {
        if (year < 1895) throw new IllegalArgumentException("The year of the movie cannot be before 1895");
        if (year > Year.now().getValue()) throw new IllegalArgumentException("The year of the movie can not be in the future");
        if (duration <= 0) throw new IllegalArgumentException("The duration of the movie must be greater than zero");
        if (null == movieId || movieId.isEmpty()) throw new IllegalArgumentException("The movieId must not be null or empty");
        if (null == title || title.isEmpty()) throw new IllegalArgumentException("The title of the movie must not be null or empty");

        // set some defaults if null is provided
        String prod = null == production ? "" : production;
        String c = null == country ? "" : country;

        GraphTraversal traversal = this.clone().V();

        // performs a "get or create/update" for the movie vertex. if it is present then it simply returns the existing
        // movie and updates the mutable property keys. if it is not, then it is created with the specified movieId
        // and properties.
        return traversal.
                has(VERTEX_MOVIE, KEY_MOVIE_ID, movieId).
                fold().
                coalesce(__.unfold(),
                         __.addV(VERTEX_MOVIE).property(KEY_MOVIE_ID, movieId)).
                property(KEY_TITLE, title).
                property(KEY_COUNTRY, c).
                property(KEY_PRODUCTION, prod).
                property(KEY_YEAR, year).
                property(KEY_DURATION, duration);
    }
}
