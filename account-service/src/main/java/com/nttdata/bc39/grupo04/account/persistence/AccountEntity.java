package com.nttdata.bc39.grupo04.account.persistence;

import com.nttdata.bc39.grupo04.api.account.HolderDTO;
import com.nttdata.bc39.grupo04.api.account.SignatoryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "accounts")
public class AccountEntity {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String account;
    private String productId;
    private String customerId;
    private List<HolderDTO> holders;
    private List<SignatoryDTO> signatories;
    private double availableBalance;
    private String debitCardNumber;
    private Date debitCardCreationDate;
    private Date createDate;
    private Date modifyDate;
    private boolean hasMinAmountDailyAverage;
    private double minAmountDailyAverage;
    private boolean hasMaintenanceFee;
    private double maintenanceFee;
    private int maxMovements;
}
