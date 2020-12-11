package com.example.guardiannewssearch;

public class Result {
    private final String title;
    private final String url;
    private final String section;
    private final long ID;
    public Result( String title, String section, String url, long ID ){
        this.section = section;
        this.title = title;
        this.url = url;
        this.ID = ID;
    }
    public String getSection(){
        return section;
    }
    public String getTitle(){
        return title;
    }
    public String getUrl(){
        return url;
    }
    public long getID(){
        return ID;
    }
}
