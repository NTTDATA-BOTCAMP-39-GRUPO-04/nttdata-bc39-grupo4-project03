package com.nttdata.bc39.grupo04.api.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {

	private static final long serialVersionUID = 3394199549879214551L;
	private String account;
    private String productId;
    private String customerId;
    private List<HolderDTO> holders;
    private List<SignatoryDTO> signatories;
    private double availableBalance;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date modifyDate;
    private boolean hasMinAmountDailyAverage;
    private double minAmountDailyAverage;
    private boolean hasMaintenanceFee;
    private double maintenanceFee;
    private int maxMovements;
    

    public String getAccount() {
        return account;
    }
}
