package com.group4.library.policy;

import com.group4.library.model.ReaderType;
import org.springframework.stereotype.Component;

@Component
public class FinePolicyFactory {
    public FinePolicy getPolicy(ReaderType type) {
        if (type == null) throw new IllegalArgumentException("Loại bạn đọc không hợp lệ");
        return switch (type) {
            case STUDENT -> new NormalStudentFinePolicy();
            case PRIORITY_STUDENT -> new PriorityStudentFinePolicy();
            case LECTURER -> new LecturerFinePolicy();
        };
    }
}
