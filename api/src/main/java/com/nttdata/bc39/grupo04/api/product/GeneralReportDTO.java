package com.nttdata.bc39.grupo04.api.product;

import java.util.List;

import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralReportDTO {

	private ProductDTO product;
	private List<MovementsReportDTO> movements;

}
