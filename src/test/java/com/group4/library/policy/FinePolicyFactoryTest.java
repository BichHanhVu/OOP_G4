package com.group4.library.policy;

import com.group4.library.model.ReaderType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinePolicyFactoryTest {

    private final FinePolicyFactory factory = new FinePolicyFactory();

    @ParameterizedTest(name = "[{index}] loại bạn đọc = {0}")
    @EnumSource(ReaderType.class)
    void getPolicy_traVeDungLoaiChoTungReaderType(ReaderType type) {
        FinePolicy policy = factory.getPolicy(type);
        assertNotNull(policy);

        switch (type) {
            case STUDENT -> assertTrue(policy instanceof NormalStudentFinePolicy);
            case PRIORITY_STUDENT -> assertTrue(policy instanceof PriorityStudentFinePolicy);
            case LECTURER -> assertTrue(policy instanceof LecturerFinePolicy);
        }
    }

    @Test
    void getPolicy_loaiNull_nemIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> factory.getPolicy(null));
    }

    @Test
    void getPolicy_moiLanGoi_traVeMotThuThuMoi() {
        // Factory không cache instance — mỗi lần gọi trả về policy mới, độc lập trạng thái.
        FinePolicy first = factory.getPolicy(ReaderType.STUDENT);
        FinePolicy second = factory.getPolicy(ReaderType.STUDENT);
        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.getClass(), second.getClass());
    }
}