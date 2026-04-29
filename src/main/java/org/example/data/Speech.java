package org.example.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This Class represents a single speech in a parliament protocol.
 */
public class Speech {
    private String id;
    private String memberId;
    private String text;
    private List<Comment> comment;

    // Metadata
    private String date;
    private int sessionNumber;
    private int legislativePeriod;

    public Speech(){this.comment = new ArrayList<Comment>();}

    // Getter and Setter

    public String getId(){return id;}
    public void setId(String id){this.id = id;}
    public String getMemberId(){return memberId;}
    public void setMemberId(String memberId){this.memberId = memberId;}
    public String getText(){return text;}
    public void setText(String text){this.text = text;}
    public List<Comment> getComment(){return comment;}
    public void setComment(List<Comment> comment){this.comment = comment;}

    public String getDate(){return date;}
    public void setDate(String date){this.date = date;}
    public int getSessionNumber(){return sessionNumber;}
    public void setSessionNumber(int sessionNumber){this.sessionNumber = sessionNumber;}
    public int getLegislativePeriod(){return legislativePeriod;}
    public void setLegislativePeriod(int legisticalPeriod){this.legislativePeriod = legisticalPeriod;}
}
