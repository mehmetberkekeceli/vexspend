package com.wallet.vexspend.dto.recurring;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessRecurringResult {

    private LocalDate processedDate;

    private int processedTemplateCount;

    private List<UUID> createdTransactionIds;

    public LocalDate processedDate() {
        return processedDate;
    }

    public int processedTemplateCount() {
        return processedTemplateCount;
    }

    public List<UUID> createdTransactionIds() {
        return createdTransactionIds;
    }

}

