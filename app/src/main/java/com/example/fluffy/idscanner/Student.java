package com.example.fluffy.idscanner;

import java.io.Serializable;

public class Student implements Serializable{
    private String id;
    private String firstName;
    private String lastName;
    private String note;
    private boolean checkedIn;

    public Student(String id, String fName, String lName, String note, boolean checkIn) {
        this.id = id;
        firstName = fName;
        lastName = lName;
        this.note = note;
        checkedIn = checkIn;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setFirst(String first){
        firstName = first;
    }
    public void setLast(String last){
        lastName = last;
    }
    public void setNote(String note){
        this.note = note;
    }
    public void setChecked(boolean checked){
        this.checkedIn = checked;
    }

    public String getId(){
        return id;
    }
    public String getFirst(){
        return firstName;
    }
    public String getLast(){
        return lastName;
    }
    public String getNote(){
        return note;
    }
    public boolean getChecked(){ return checkedIn; }
}
