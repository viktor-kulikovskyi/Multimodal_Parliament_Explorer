package org.example.data;


import java.util.Map;

/**
 * This Class represent the Member of Parliament
 */
public class MemberOfParlament {
    private String id;
    private String firstname;
    private String lastname;
    private String fraction;
    private String fotoUrl;
    private Map<String, String> metaData; // For additional metadata such as date of birth

    public MemberOfParlament() {}

    // Getters and Setters
    public String getId(){return id;}
    public void setId(String id){this.id = id;}

    public String getFirstname(){return firstname;}
    public void setFirstname(String firstname){this.firstname = firstname;}

    public String getLastname(){return lastname;}
    public void setLastname(String lastname){this.lastname = lastname;}

    public String getFraction(){return fraction;}
    public void setFraction(String fraction){this.fraction = fraction;}

    public String getFotoUrl(){return fotoUrl;}
    public void setFotoUrl(String fotoUrl){this.fotoUrl = fotoUrl;}

    public Map<String, String> getMetaData(){return metaData;}
    public void setMetaData(Map<String, String> metaData){this.metaData = metaData;}
}
