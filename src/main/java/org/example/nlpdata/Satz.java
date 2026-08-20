package org.example.nlpdata;

import org.bson.Document;
/**
 * The type Satz.
 */
public class Satz{
    /**
     * Instantiates a new Satz.
     */
    private Document document;
    public Satz(){};
    /**
     * The Start.
     */
    public    int start;
    /**
     * The Ende.
     */
    public    int ende;
    /**
     * The Inhalt.
     */
    public    String inhalt;
    /**
     * The Sentiment.
     */
    public    double sentiment;

    /**
     * Gets start.
     *
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets start.
     *
     * @param s the s
     */
    public void setStart(int s) {start=s;}
    public void  setEnd(int e) {ende=e;}
    /**
     * Gets ende.
     *
     * @return the ende
     */
    public int getEnde() {
        return ende;
    }

    /**
     * Gets inhalt.
     *
     * @return the inhalt
     */
    public String getInhalt() {
        return inhalt;
    }

    /**
     * Sets inhalt.
     *
     * @param s the s
     */
    public void setInhalt(String s) {inhalt=s;}

    /**
     * Gets sentiment.
     *
     * @return the sentiment
     */
    public double getSentiment() {
        return sentiment;
    }
    public Document toDocument(){
        return new Document()
                .append("start",start)
                .append("ende",ende)
                .append("inhalt", inhalt)
                .append("sentiment", sentiment);
    }

}
