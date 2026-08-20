package org.example.nlpdata;

import org.bson.Document;


/**
 * The type Parts os.
 */
public class PartsOS {
    private Document document;
    /**
     * The Start.
     */
    public int start;
    /**
     * The End.
     */
    public int end;
    /**
     * The Value.
     */
    public String value;
    /**
     * The Inhalt.
     */
    public String inhalt;
    public void setStart(int start) {
        this.start = start;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }
    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }
    public String getValue() {
        return value;
    }
    public String getInhalt() {
        return inhalt;
    }
    public Document toDocument() {
        return new Document()
                .append("start", this.start)
                .append("end", this.end)
                .append("value", this.value)
                .append("Inhalt", this.inhalt);
    }

}
