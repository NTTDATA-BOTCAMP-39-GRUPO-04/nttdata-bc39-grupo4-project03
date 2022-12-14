package com.nttdata.bc39.grupo04.api.account;

import java.util.List;

import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitCardReportDTO {
	
    private String debitCardNumber;
    private List<MovementsReportDTO> movements;

}
