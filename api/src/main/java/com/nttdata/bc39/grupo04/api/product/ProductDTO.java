package com.nttdata.bc39.grupo04.api.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nttdata.bc39.grupo04.api.product.enumerator.Enum;
import com.nttdata.bc39.grupo04.api.product.enumerator.TypeProductEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO implements Serializable {


    private static final long serialVersionUID = 8242254604761455533L;

    @JsonProperty("code")
    @NotBlank(message = "Error, código de producto inválido")
    private String code;

    @JsonProperty("name")
    @NotBlank(message = "Error, nombre de producto inválido")
    private String name;

    @JsonProperty("typeProduct")
    @NotBlank(message = "Error, tipo de producto inválido")
    @Enum(enumClass = TypeProductEnum.class, ignoreCase = true)
    private String typeProduct;
}
