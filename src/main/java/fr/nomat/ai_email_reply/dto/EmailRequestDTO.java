package fr.nomat.ai_email_reply.dto;

import lombok.Data;

@Data
public class EmailRequestDTO {
    private String emailContent;
    private String tone;
}
