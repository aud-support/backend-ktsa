package com.ktsa.foosball.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkImportResultDto {
    private int totalRows;
    private int created;
    private int skipped;
    private List<String> skippedEmails; // emails that already existed or had errors
}
