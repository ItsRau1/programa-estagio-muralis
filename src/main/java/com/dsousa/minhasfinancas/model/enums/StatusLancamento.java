package com.dsousa.minhasfinancas.model.enums;

import lombok.Getter;

@Getter
public enum StatusLancamento {

	PENDENTE("PENDENTE"), CANCELADO("CANCELADO"), EFETIVADO("EFETIVADO");

	private final String descricao;

	StatusLancamento(String descricao) {
		this.descricao = descricao;
	}

}
