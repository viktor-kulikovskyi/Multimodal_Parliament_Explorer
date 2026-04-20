package org.example.data;

/**
 * This class represents a Topic in a Speech.
 */
public class Topic {

    private String name;
    private double score; // To measure probability of the topic.

    public Topic(){}

    public Topic(String name, double score) {
        this.name = name;
        this.score = score;
    }

    // Getter and Setter

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
