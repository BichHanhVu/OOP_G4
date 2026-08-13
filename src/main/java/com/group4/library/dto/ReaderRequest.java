package com.group4.library.dto;

public class ReaderRequest {
    private String id;          // optional — nếu để trống, server tự sinh mã
    private String name;
    private String phoneNumber;
    private String type;        // "STUDENT" | "PRIORITY_STUDENT" | "LECTURER"

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
