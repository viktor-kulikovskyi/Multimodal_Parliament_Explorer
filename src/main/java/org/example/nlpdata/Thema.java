package org.example.nlpdata;

import org.bson.Document;
/**
 * The type Thema.
 */
public class Thema{
    private Document document;
    /**
     * The Start.
     */
    public int start;
    /**
     * The Ende.
     */
    public int ende;
    /**
     * The Value.
     */
    public String value;
    /**
     * The Score.
     */
    public double score;

    public void  setStart(int start){
        this.start = start;
    }
    public int getStart(){
        return this.start;
    }
    public void setEnde(int ende){
        this.ende = ende;
    }
    public int getEnde(){
        return this.ende;
    }
    public void setValue(String value){
        this.value = value;
    }
    public String getValue(){
        return this.value;
    }
    public void setScore(double score){
        this.score = score;

    }
    public double getScore(){
        return this.score;
    }
    public Document toDocument()
    {
        return new Document()
                .append("start", this.start)
                .append("ende", this.ende)
                .append("value", this.value)
                .append("score", this.score);
    }
}
