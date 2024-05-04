package com.killrvideo;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.GremlinDsl;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.P;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.killrvideo.KV.EDGE_ACTOR;
import static com.killrvideo.KV.EDGE_BELONGS_TO;
import static com.killrvideo.KV.EDGE_RATED;
import static com.killrvideo.KV.KEY_AGE;
import static com.killrvideo.KV.KEY_NAME;
import static com.killrvideo.KV.KEY_PERSON_ID;
import static com.killrvideo.KV.KEY_RATING;
import static com.killrvideo.KV.VERTEX_PERSON;
import static com.killrvideo.Recommender.SMALL_SAMPLE;
import static org.apache.tinkerpop.gremlin.process.traversal.Order.decr;
import static org.apache.tinkerpop.gremlin.process.traversal.P.gt;
import static org.apache.tinkerpop.gremlin.process.traversal.P.lt;
import static org.apache.tinkerpop.gremlin.process.traversal.P.without;
import static org.apache.tinkerpop.gremlin.process.traversal.P.between;
import static org.apache.tinkerpop.gremlin.process.traversal.Scope.local;
import static org.apache.tinkerpop.gremlin.structure.Column.keys;
import static org.apache.tinkerpop.gremlin.structure.Column.values;

/**
 * The KillrVideo DSL definition that produces the custom {@code GraphTraversal} and specifies the custom
 * {@code GraphTraversalSource}.
 * <p/>
 * A DSL definition must be an interface and extend {@code GraphTraversal.Admin} and should be annotated with the
 * {@code GremlinDsl} annotation. Methods that are added to this interface become steps that are "appended" to the
 * common steps of the Gremlin language. These methods must:
 * <ul>
 *     <li>Return a {@code GraphTraversal}</li>
 *     <li>Use common Gremlin steps or other DSL steps to compose the returned {@code GraphTraversal}</li>
 * </ul>
 * These methods are only applied to a {@code GraphTraversal}, but recall that a {@code GraphTraversal} is spawned
 * from a {@code GraphTraversalSource}. To be clear, the "g" in {@code g.V()} is a {@code GraphTraversalSource} and
 * the {@code V()} is a start step. To include DSL-based start steps on a custom {@code GraphTraversalSource} the
 * "traversalSource" parameter is supplied to the {@code GremlinDsl} annotation which specifies the fully qualified
 * name of the class that contains those DSL-based start steps.
 */
@GremlinDsl(traversalSource = "com.killrvideo.KillrVideoTraversalSourceDsl")
public interface KillrVideoTraversalDsl<S, E> extends GraphTraversal.Admin<S, E> {

    /**
     * Traverses from a "movie" to an "person" over the "actor" edge.
     */
    public default GraphTraversal<S, Vertex> actors() {
        return out(EDGE_ACTOR).hasLabel(VERTEX_PERSON);
    }

    /**
     * Traverses from a "movie" to a "rated" edge.
     */
    public default GraphTraversal<S, Edge> ratings() {
        return inE(EDGE_RATED);
    }

    /**
     * Calls {@link #rated(int, int)} with both arguments as zero.
     */
    public default GraphTraversal<S, Vertex> rated() {
        return rated(0,0);
    }

    /**
     * Traverses from a "user" to a "movie" over the "rated" edge, filtering those edges as specified. If both arguments
     * are zero then there is no rating filter.
     *
     * @param min minimum rating to consider
     * @param max maximum rating to consider
     */
    public default GraphTraversal<S, Vertex> rated(int min, int max) {
        if (min < 0 || max > 10) throw new IllegalArgumentException("min must be a value between 0 and 10");
        if (max < 0 || max > 10) throw new IllegalArgumentException("min must be a value between 0 and 10");
        if (min != 0 && max != 0 && min > max) throw new IllegalArgumentException("min cannot be greater than max");

        if (min == 0 && max == 0)
            return out(EDGE_RATED);
        else if (min == 0)
            return outE(EDGE_RATED).has(KEY_RATING, gt(min)).inV();
        else if (max == 0)
            return outE(EDGE_RATED).has(KEY_RATING, lt(min)).inV();
        else
            return outE(EDGE_RATED).has(KEY_RATING, between(min, max)).inV();
    }

    /**
     * Assumes a "movie" vertex and traverses to a "genre" vertex with a filter on the name of the genre. This
     * step is meant to be used as part of a <code>filter()</code> step for movies.
     */
    public default GraphTraversal<S, Vertex> genre(final Genre genre, final Genre... additionalGenres) {
        List<String> genres = Stream.concat(Stream.of(genre), Stream.of(additionalGenres))
                .map(Genre::getName)
                .collect(Collectors.toList());

        if (genres.size() < 1)
            throw new IllegalArgumentException("There must be at least one genre option provided");

        if (genres.size() == 1)
            return out(EDGE_BELONGS_TO).has(KEY_NAME, genres.get(0));
        else
            return out(EDGE_BELONGS_TO).has(KEY_NAME, P.within(genres));
    }

    /**
     * Assumes incoming "rated" edges and filters based on the age of the "user" enforcing the logic that the
     * {@code start} age should exclude minors (i.e. 18 and older). Produces a map where the key is the rating and
     * the value is the number of times that rating was given.
     *
     * @param start the start age which must be 18 or greater
     * @param end the end age
     * @return
     */
    public default GraphTraversal<S, Map<Object,Object>> distributionForAges(int start, int end) {
        if (start < 18) throw new IllegalArgumentException("Age must be 18 or older");
        if (start > end) throw new IllegalArgumentException("Start age must be greater than end age");
        if (end > 120) throw new IllegalArgumentException("Now you're just being crazy");

        return filter(__.outV().has(KEY_AGE, between(start,end))).group().by(KEY_RATING).by(__.count());
    }

    /**
     * A convenience overload for {@link #recommend(int, int, Recommender, Traversal)}.
     *
     * @param recommendations the number of recommended movies to return
     * @param minRating the minimum rating to allow for
     */
    public default GraphTraversal<S, Vertex> recommend(int recommendations, int minRating) {
        if (recommendations <= 0) throw new IllegalArgumentException("recommendations must be greater than zero");

        return recommend(recommendations, minRating, SMALL_SAMPLE, __.__());
    }

    /**
     * A convenience overload for {@link #recommend(int, int, Recommender, Traversal)}.
     *
     * @param recommendations the number of recommended movies to return
     * @param minRating the minimum rating to allow for
     */
    public default GraphTraversal<S, Vertex> recommend(int recommendations, int minRating, Traversal include) {
        if (recommendations <= 0) throw new IllegalArgumentException("recommendations must be greater than zero");

        return recommend(recommendations, minRating, SMALL_SAMPLE, include);
    }

    /**
     * A simple recommendation algorithm that starts from a "user" and examines movies the user has seen filtered by
     * the {@code minRating} which removes movies that hold a rating lower than the value specified. It then samples
     * the actors in the movies the user has seen and uses that to find other movies those actors have been in that
     * the user has not yet seen. Those movies are grouped, counted and sorted based on that count to produce the
     * recommendation.
     *
     * @param recommendations the number of recommended movies to return
     * @param minRating the minimum rating to allow for
     * @param recommender a configuration to supply to the recommendation algorithm to provide control over how
     *                    sampling occurs when selecting the initial set of movies to consider
     * @param include an anonymous traversal that must be "true" for the incoming "movie" vertex to be included in the
     *                recommendation results
     */
    public default GraphTraversal<S, Vertex> recommend(int recommendations, int minRating, Recommender recommender, Traversal include) {
        if (recommendations <= 0) throw new IllegalArgumentException("recommendations must be greater than zero");

        return rated(minRating, 0).
                aggregate("seen").
                local(recommender.getTraversal()).
                unfold().in(EDGE_ACTOR).
                where(without("seen")).
                where(include).
                groupCount().
                order(local).
                  by(values, decr).
                limit(local,recommendations).
                select(keys).
                unfold();
    }

    /**
     * Gets or creates a "person".
     * <p/>
     * This step first checks for existence of a person given their identifier. If it exists then the person is
     * returned and their "name" property updated. It is not possible to change the person's identifier once it is
     * assigned (at least as defined by this DSL). If the person does not exist then a new person vertex is added
     * with the specified identifier and name.
     */
    public default GraphTraversal<S, Vertex> person(String personId, String name) {
        if (null == personId || personId.isEmpty()) throw new IllegalArgumentException("The personId must not be null or empty");
        if (null == name || name.isEmpty()) throw new IllegalArgumentException("The name of the person must not be null or empty");

        return coalesce(__.V().has(VERTEX_PERSON, KEY_PERSON_ID, personId),
                        __.addV(VERTEX_PERSON).property(KEY_PERSON_ID, personId)).
                property(KEY_NAME, name);
    }

    /**
     * Gets or creates an "actor".
     * <p/>
     * In this schema, an actor is a "person" vertex with an incoming "actor" edge from a "movie" vertex. This step
     * therefore assumes that the incoming stream is a "movie" vertex and actors will be attached to that. This step
     * checks for existence of the "actor" edge first before adding and if found will return the existing one. It
     * further ensures the existence of the "person" vertex as provided by the {@link #person(String, String)}
     * step.
     */
    public default GraphTraversal<S, Vertex> actor(String personId, String name) {
        // no validation here as it would just duplicate what is happening in person(). note the use of the
        // cast to KillrVideoTraversal. in this case, we want to use a DSL step within the DSL itself, but we want to
        // start the traversal with a GraphTraversal step which thus returns a GraphTraversal. The only ways to get
        // around this is to do the cast or to create a version of the GraphTraversal step in the DSL that will
        // provide such access and return a "KillrVideoTraversal".
        //
        // as mentioned in the javadocs this step assumes an incoming "movie" vertex. it is immediately labelled as
        // "^movie". the addition of the caret prefix has no meaning except to provide for a unique labelling space
        // within the DSL itself.
        return ((KillrVideoTraversal) as("^movie")).
                coalesce(__.actors().has(KEY_PERSON_ID, personId),
                         __.person(personId, name).addE(EDGE_ACTOR).from("^movie").inV());
    }

    /**
     * This step is an alias for the {@code sideEffect()} step. As an alias, it makes certain aspects of the DSL more
     * readable.
     */
    public default GraphTraversal<S,?> ensure(Traversal<?,?> mutationTraversal) {
        return sideEffect(mutationTraversal);
    }
}
