package com.group4.library.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** User là abstract nên dùng StudentReader làm đại diện cụ thể để kiểm tra validate của lớp cha. */
class UserValidationTest {

    @Test
    void tenRong_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", "   ", "0912345678"));
    }

    @Test
    void tenNull_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", null, "0912345678"));
    }

    @Test
    void sdtSaiDinhDang_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", "Nguyễn Văn A", "abc123"));
    }

    @Test
    void sdtNull_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", "Nguyễn Văn A", null));
    }

    @Test
    void sdtQuaNgan_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", "Nguyễn Văn A", "123"));
    }

    @Test
    void sdtQuaDai_nemLoi() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudentReader("R001", "Nguyễn Văn A", "123456789012"));
    }
}