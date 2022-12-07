package com.nttdata.bc39.grupo04.product.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "product")
public class ProductEntity {
	
	private ObjectId id;
	
	private String code;
	
	private String name;
	
	private String typeProduct;

}
