package org.example.nlpdata;

import org.bson.Document;
public class Tokendata {
    private Document document;

    public int     start;
    public int    ende;
    public String inhalt;

    public void setEnde(int ende) {
        this.ende = ende;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;

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
    public Document toDocument()
    {
        return new Document()
                .append("start", this.start)
                .append("ende", this.ende)
                .append("inhalt", this.inhalt);
    }}
