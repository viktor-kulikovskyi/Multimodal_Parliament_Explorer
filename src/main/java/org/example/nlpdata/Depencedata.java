package org.example.nlpdata;

import org.bson.Document;
public class Depencedata {

    public int begin;
    public int end;
    public String governor ;
    public String dependent;
    public String dependencytype;
    public void setEnd(int end) {
        this.end = end;
    }
    public void setBegin(int begin) {}
    public void setDependent(String dependent) {
        this.dependent = dependent;
    }
    public void setGovernor(String governer) {
        this.governor = governer;
    }
    public void setDependencyType(String dependencytype) {
        this.dependencytype = dependencytype;
    }
    public int getBegin() {
        return begin;
    }
    public int getEnd() {
        return end;
    }
    public String getGovernor() {
        return governor;
    }
    public String getDependent() {
        return dependent;
    }
    public String getDependencyType() {
        return dependencytype;
    }
    public Document toDocument() {
        return new Document()
                .append("begin", this.begin)
                .append("end", this.end)
                .append("governer", this.governor)
                .append("dependent", this.dependent)
                .append("dependencytype", this.dependencytype);
    }
}