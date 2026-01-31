package com.hireme.authservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EmailDto {
    private String sentTo;
    private String subject;
    private String textBody;
}
