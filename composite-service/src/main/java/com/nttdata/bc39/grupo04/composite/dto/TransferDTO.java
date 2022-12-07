package com.nttdata.bc39.grupo04.composite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferDTO {
  String sourceAccount;
  String destinationAccount;
  double amount;
  double commision;
  double availableBalance;
}
