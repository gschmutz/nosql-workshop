package com.killrvideo;

import org.apache.tinkerpop.gremlin.process.traversal.Traversal;

import static com.killrvideo.KV.EDGE_ACTOR;

/**
 * Provides for pre-built "sampling" settings to the {@link KillrVideoTraversalDsl#recommend(int, int, Recommender, Traversal)}
 * step. The sampling options help determine the nature of the initial set of movies to recommend, by limiting the
 * number of actors used from highly rated movies of the user who is target for the recommendation.
 */
public enum Recommender {

    /**
     * Sample three actors.
     */
    SMALL_SAMPLE(__.outE(EDGE_ACTOR).sample(3).inV().fold()),

    /**
     * Sample ten actors.
     */
    LARGE_SAMPLE(__.outE(EDGE_ACTOR).sample(10).inV().fold()),

    /**
     * Iterate all actors taking roughly 50% of them.
     */
    FIFTY_50_SAMPLE(__.outE(EDGE_ACTOR).coin(0.5d).inV().fold()),

    /**
     * For each rated movie take actors for 250ms.
     */
    TIMED_SAMPLE(__.outE(EDGE_ACTOR).timeLimit(250).inV().fold()),

    /**
     * Do not sample and use all the actors.
     */
    ALL(__.outE(EDGE_ACTOR).inV().fold());

    private Traversal<?, ?> t;

    Recommender(Traversal t) {
        this.t = t;
    }

    public Traversal<?, ?> getTraversal() {
        return t.asAdmin().clone();
    }
}
