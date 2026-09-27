package com.agri.silo.dto;

import com.agri.silo.enums.JournalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalDTO {
    private Long id;
    private String code;
    private String name;
    private JournalType type;
}
