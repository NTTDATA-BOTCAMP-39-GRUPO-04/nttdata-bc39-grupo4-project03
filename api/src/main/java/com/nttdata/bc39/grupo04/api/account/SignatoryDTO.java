package com.nttdata.bc39.grupo04.api.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignatoryDTO implements Serializable {
    private String code;
    private String name;
}
