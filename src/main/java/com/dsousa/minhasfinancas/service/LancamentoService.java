package com.dsousa.minhasfinancas.service;

import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LancamentoService {

	Lancamento salvar(Lancamento lancamento);

	void atualizar(Lancamento lancamento);

	void deletar(Lancamento lancamento);

	List<Lancamento> buscar(Lancamento lancamentoFiltro);

	void atualizarStatus(Lancamento lancamento, StatusLancamento status);

	Optional<Lancamento> obterPorId(Long id);

	BigDecimal obterSaldoPorUsuario(Long id);

}