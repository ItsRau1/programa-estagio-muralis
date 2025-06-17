package com.dsousa.minhasfinancas.model.repository;

import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;
import com.dsousa.minhasfinancas.model.enums.TipoLancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

	@Query(value = "SELECT SUM(l.valor) FROM Lancamento l JOIN l.usuario u "
			+ "WHERE u.id = :idUsuario AND l.tipo =:tipo AND l.status = :status GROUP BY u")
	BigDecimal obterSaldoPorTipoLancamentoEUsuarioEStatus(@Param("idUsuario") Long idUsuario,
			@Param("tipo") TipoLancamento tipo, @Param("status") StatusLancamento status);

}
