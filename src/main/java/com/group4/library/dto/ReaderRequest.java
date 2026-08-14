// dto/ReaderRequest.java
package com.group4.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ReaderRequest {
    private String id; // optional — nếu để trống, server tự sinh mã

    @NotBlank(message = "Họ tên không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "\\d{9,11}", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Loại bạn đọc không được để trống")
    private String type; // "STUDENT" | "PRIORITY_STUDENT" | "LECTURER"

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}