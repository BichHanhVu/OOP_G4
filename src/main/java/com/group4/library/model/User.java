// model/User.java
package com.group4.library.model;

public abstract class User {
    private String id;
    private String name;
    private String phoneNumber;

    public User(String id, String name, String phoneNumber) {
        setId(id);
        setName(name);
        setPhoneNumber(phoneNumber);
    }

    public String getId() { return id; }
    private void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã bạn đọc không được để trống");
        }
        this.id = id.trim();
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        this.name = name.trim();
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{9,11}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
        this.phoneNumber = phoneNumber;
    }
}