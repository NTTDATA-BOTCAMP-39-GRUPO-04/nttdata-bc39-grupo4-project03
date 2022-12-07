package com.nttdata.bc39.grupo04.credit.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "credit")
public class CreditEntity {

    private ObjectId id;
    @Indexed(unique = true)
    private String creditNumber;
    private String productId;
    private String customerId;
    private double availableBalance;
    private double creditAmount;
    private String cardNumber;
    private Date createDate;
    private Date modifyDate;

}
