package org.example.data;

import java.util.ArrayList;
import java.util.List;

public class AgendaItem {

    private String title;
    private int number;
    private List<Speech> speechList;

    public  AgendaItem() {
        this.speechList = new ArrayList<>();
    }

    // Getter and Setter

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public List<Speech> getSpeechList() { return speechList; }
    public void setSpeechList(List<Speech> speechList) { this.speechList = speechList; }
}
