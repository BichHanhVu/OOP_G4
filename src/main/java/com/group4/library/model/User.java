package com.group4.library.model;

import java.util.Objects;

/** Lớp cha trừu tượng cho mọi loại người dùng trong hệ thống. */
public abstract class User {
    private String id;
    private String name;
    private String phoneNumber;

    public User(String id, String name, String phoneNumber) {
        this.id = id;
        setName(name);
        setPhoneNumber(phoneNumber);
    }

    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống");
        }
        this.name = name;
    }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{9,11}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ");
        }
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User user = (User) other;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name + ", phoneNumber=" + phoneNumber;
    }
}