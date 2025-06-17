package com.dsousa.minhasfinancas.model.enums;

import lombok.Getter;

@Getter
public enum TipoLancamento {

	RECEITA("RECEITA"),
	DESPESA("DESPESA");

	private final String descricao;

	TipoLancamento(String descricao) {
		this.descricao = descricao;
	}
}
