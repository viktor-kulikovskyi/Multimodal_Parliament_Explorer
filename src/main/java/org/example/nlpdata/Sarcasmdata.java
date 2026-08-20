package org.example.nlpdata;

import org.bson.Document;
public class Sarcasmdata {

    private int start;
    private int ende;
    private String inhalt;
    private double sarcasmScore;
    private double nonSarcasmScore;

    public void  setStart(int start) {
        this.start = start;
    }
    public void setEnde(int ende) {
        this.ende = ende;
    }
    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }
    public void setSarcasmScore(double sarcasmScore) {
        this.sarcasmScore = sarcasmScore;

    }
    public void setNonSarcasmScore(double nonSarcasmScore) {
        this.nonSarcasmScore = nonSarcasmScore;
    }
    public int getStart() {
        return start;
    }
    public int getEnde() {
        return ende;
    }
    public String getInhalt() {
        return inhalt;
    }
    public double getSarcasmScore() {
        return sarcasmScore;
    }
    public double getNonSarcasmScore() {
        return nonSarcasmScore;
    }
    public Document toDocument()
    {
        return new Document()
                .append("start", this.start)
                .append("ende", this.ende)
                .append("inhalt", this.inhalt)
                .append("sarcasmScore", this.sarcasmScore)
                .append("nonSarcasmScore", this.nonSarcasmScore);
    }

}