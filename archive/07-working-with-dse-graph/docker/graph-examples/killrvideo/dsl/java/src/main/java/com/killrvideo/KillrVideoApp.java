package com.killrvideo;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.dse.graph.api.DseGraph;
import com.datastax.dse.graph.internal.DseRemoteConnection;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.killrvideo.Genre.COMEDY;
import static com.killrvideo.KV.KEY_TITLE;
import static com.killrvideo.Recommender.LARGE_SAMPLE;
import static com.killrvideo.__.actor;
import static com.killrvideo.__.genre;

/**
 * A simple console application which demonstrates some of the features of the KillrVideo DSL.
 */
public final class KillrVideoApp {

    public static void main(String [] args) {
        DseCluster dseCluster = DseCluster.builder()
                .addContactPoint("127.0.0.1")
                .build();
        DseSession dseSession = dseCluster.connect();

        try {
            // initialize the TraversalSource for the DSL using the DSE Java Driver
            // https://github.com/datastax/java-dse-driver
            KillrVideoTraversalSource killr = DseGraph.traversal(dseSession,
                    new GraphOptions().setGraphName("killrvideo"), KillrVideoTraversalSource.class);

            printHeader("Actors for Young Guns", "killr.movies(\"Young Guns\").actors().values(\"name\")");
            List<Object> names = killr.movies("Young Guns").actors().values("name").toList();
            names.forEach(System.out::println);

            printHeader("Ratings Distribution by Age for Young Guns", "killr.movies(\"Young Guns\").ratings().distributionForAges(18, 40)");
            Map ratingsByAge = killr.movies("Young Guns").ratings().distributionForAges(18, 40).next();
            System.out.println(ratingsByAge);

            printHeader("Failed Validation", "killr.movies(\"Young Guns\").ratings().distributionForAges(17,40)");
            try {
                killr.movies("Young Guns").ratings().distributionForAges(17,40).next();
            } catch (IllegalArgumentException iae) {
                System.out.println("Caught IllegalArgumentException: " + iae.getMessage());
                Thread.sleep(500);
            }

            printHeader("Five Recommendations for u460", "killr.users(\"u460\").recommend(5, 7).values(KEY_TITLE)");
            killr.users("u460").recommend(5, 7).values(KEY_TITLE).forEachRemaining(System.out::println);

            printHeader("Five Recommendations for u460 that are comedies", "killr.users(\"u460\").recommend(5, 7, genre(COMEDY)).values(KEY_TITLE)");
            killr.users("u460").recommend(5, 7, genre(COMEDY)).values(KEY_TITLE).forEachRemaining(System.out::println);

            printHeader("Five Recommendations for u460 that use larger actor sampling and are comedies", "killr.users(\"u460\").recommend(5, 7, genre(COMEDY)).values(KEY_TITLE)");
            killr.users("u460").recommend(5, 7, LARGE_SAMPLE, genre(COMEDY)).values(KEY_TITLE).forEachRemaining(System.out::println);

            printHeader("Insert/update movie and a actors for that movie", "killr.movie(\"m100000\", \"Manos: The Hands of Fate\",...).actor(...)");
            killr.movie("m100000", "Manos: The Hands of Fate", "USA", "Sun City Films", 1966, 70).
                    ensure(actor("p1000000", "Tom Neyman")).
                    ensure(actor("p1000001", "John Reynolds")).
                    ensure(actor("p1000002", "Diane Mahree")).
                    forEachRemaining(v -> System.out.println("Added 3 actors to 'Manos: The Hands of Fate'"));

            printHeader("Get the actors for the newly added movie", "killr.movies(\"Manos: The Hands of Fate\").actors().values(\"name\")");
            names = killr.movies("Manos: The Hands of Fate").actors().values("name").toList();
            names.forEach(System.out::println);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            dseSession.close();
            dseCluster.close();
            System.exit(0);
        }
    }

    private static void printHeader(String title) {
        printHeader(title, null);
    }

    private static void printHeader(String title, String subtitle) {
        String st = "";
        String t = String.format("\n* %s", title);
        System.out.println(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            st = String.format("[%s]", subtitle);
            System.out.println(st);
        }

        String line = String.join("", Collections.nCopies(st.isEmpty() ? t.length() : st.length(), "-"));
        System.out.println(line);
    }

}
