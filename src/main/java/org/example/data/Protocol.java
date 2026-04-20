package org.example.data;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a plenary protocol of a Bundestag session.
 */
public class Protocol {

    // FÜR EXPORT - - - -- - -  -- -

    private int sessionNumber;
    private int wahlperiode;
    private String date;
    private List<AgendaItem> agendaItemList;

    public Protocol(){this.agendaItemList = new ArrayList<>();}

    // Getter and Setter

    public int getSessionNumber() {return sessionNumber;}
    public void setSessionNumber(int sessionNumber) {this.sessionNumber = sessionNumber;}

    public int getWahlperiode() {return wahlperiode;}
    public void setWahlperiode(int wahlperiode) {this.wahlperiode = wahlperiode;}

    public String getDate() {return date;}
    public void setDate(String date) {this.date = date;}

    public List<AgendaItem> getAgendaItemList() {return agendaItemList;}
    public void setAgendaItemList(List<AgendaItem> agendaItemList){
        this.agendaItemList = agendaItemList;
    }
}
