package org.example.data;

/**
 * This class represents a single comment or interjection made during a speech.
 */
public class Comment {
    private String text;
    private String author;
    private String fraction;
    public Comment(){}

    // Getter and Setter

    public String getText(){return text;}
    public void setText(String text){this.text = text;}

    public String getAuthor(){return author;}
    public void setAuthor(String author){this.author = author;}

    public String getFraction(){return fraction;}
    public void setFraction(String fraction){this.fraction = fraction;}

}
