package com.yookassa.spring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Confirmation {
    private String type;
    private String returnUrl;
    private String confirmationUrl;
}