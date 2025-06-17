package com.dsousa.minhasfinancas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class TokenDTO {

	private String nome;

	private String token;
	
}
