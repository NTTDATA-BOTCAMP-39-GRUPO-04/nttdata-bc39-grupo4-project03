package com.nttdata.bc39.grupo04.api.composite;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComissionReportDTO {
    private String productId;
    private String productName;
    private double totalAmount;
}
