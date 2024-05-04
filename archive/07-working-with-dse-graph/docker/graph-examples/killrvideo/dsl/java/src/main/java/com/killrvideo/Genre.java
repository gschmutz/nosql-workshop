package com.killrvideo;

/**
 * The available "genre" vertex types in the KillrVideo dataset.
 */
public enum Genre {

    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FANTASY("Fantasy"),
    FILM_NOIR("Film-Noir"),
    HORROR("Horror"),
    KIDS("Kids"),
    MUSICAL("Musical"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    SCI_FI("Sci-Fi"),
    TV_SERIES("TV Series"),
    THRILLER("Thriller"),
    WAR("War"),
    WESTERN("Western");

    private String name;

    Genre(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
