package com.booklibrary.dto;

public class BookRequest {
    
    private Integer id;

    public BookRequest() {
    }

    public BookRequest(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
