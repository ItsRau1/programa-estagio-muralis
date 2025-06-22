package com.dsousa.minhasfinancas.service.impl;

import com.dsousa.minhasfinancas.api.dto.LancamentoCsvResponseDTO;
import com.dsousa.minhasfinancas.api.dto.LancamentoCsvResponseDTO.ErroProcessamentoCsv;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;
import com.dsousa.minhasfinancas.model.enums.TipoLancamento;
import com.dsousa.minhasfinancas.model.repository.LancamentoRepository;
import com.dsousa.minhasfinancas.service.LancamentoService;
import com.dsousa.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LancamentoServiceImpl implements LancamentoService {

	private static final String[] CSV_HEADERS = {
		"descricao", "valor", "mes", "ano", "tipo", "status"
	};

	private final LancamentoRepository repository;
	private final UsuarioService usuarioService;

	@Override
	@Transactional
	public Lancamento salvar(Lancamento lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void atualizar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Lancamento lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Lancamento> buscar(Lancamento lancamentoFiltro) {
		return repository.findAll(Example.of(lancamentoFiltro,
				ExampleMatcher.matching().withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING)));
	}

	@Override
	@Transactional
	public void atualizarStatus(Lancamento lancamento, StatusLancamento status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
	}

	@Override
	@Transactional(readOnly = true)
	public BigDecimal obterSaldoPorUsuario(Long id) {
		BigDecimal receitas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.RECEITA,
				StatusLancamento.EFETIVADO);
		BigDecimal despesas = repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(id, TipoLancamento.DESPESA,
				StatusLancamento.EFETIVADO);
		if (receitas == null) {
			receitas = BigDecimal.ZERO;
		}
		if (despesas == null) {
			despesas = BigDecimal.ZERO;
		}
		return receitas.subtract(despesas);
	}

	@Override
	public Optional<Lancamento> obterPorId(Long id) {
		return repository.findById(id);
	}

	public void validar(Lancamento lancamento) {
		if (lancamento.getDescricao() == null || lancamento.getDescricao().trim().isEmpty()) {
			throw new RegraNegocioException("Informe uma Descrição válida.");
		}
		if (lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um Mês válido.");
		}
		if (lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um Ano válido.");
		}
		if (lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe um Usuário.");
		}
		if (lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Informe um Valor válido.");
		}
		if (lancamento.getTipo() == null) {
			throw new RegraNegocioException("Informe um Tipo de Lançamento.");
		}
	}

	@Override
	@Transactional
	public LancamentoCsvResponseDTO processarCsv(MultipartFile arquivo, Long usuarioId) {
		List<ErroProcessamentoCsv> erros = new ArrayList<>();
		List<Lancamento> lancamentosValidos = new ArrayList<>();
		int linhaAtual = 1; // Começa do 1 por causa do cabeçalho

		if (!Objects.requireNonNull(arquivo.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
			throw new RegraNegocioException("O arquivo deve ser do tipo CSV");
		}

		try (BufferedReader fileReader = new BufferedReader(
			new InputStreamReader(arquivo.getInputStream(), StandardCharsets.UTF_8));
			 CSVParser csvParser = new CSVParser(fileReader,
				CSVFormat.DEFAULT.builder()
					.setHeader(CSV_HEADERS)
					.setSkipHeaderRecord(true)
					.setIgnoreHeaderCase(true)
					.setTrim(true)
					.build())) {

			Usuario usuario = usuarioService.obterPorId(usuarioId)
				.orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

			for (CSVRecord csvRecord : csvParser) {
				linhaAtual++;
				List<String> errosLinha = new ArrayList<>();

				try {
					Lancamento lancamento = criarLancamentoAPartirCsv(csvRecord, usuario);
					validar(lancamento);
					lancamentosValidos.add(lancamento);
				} catch (RegraNegocioException e) {
					errosLinha.add(e.getMessage());
				} catch (Exception e) {
					errosLinha.add("Erro ao processar linha: " + e.getMessage());
				}

				if (!errosLinha.isEmpty()) {
					erros.add(ErroProcessamentoCsv.builder()
						.linha(linhaAtual)
						.mensagensErro(errosLinha)
						.build());
				}
			}

			// Salva todos os lançamentos válidos em lote
			if (!lancamentosValidos.isEmpty()) {
				repository.saveAll(lancamentosValidos);
			}

		} catch (IOException e) {
			throw new RegraNegocioException("Erro ao processar o arquivo: " + e.getMessage());
		}

		return LancamentoCsvResponseDTO.builder()
				.totalLinhasProcessadas(linhaAtual - 1) // Subtrai 1 para desconsiderar o cabeçalho
				.totalLinhasComErro(erros.size())
				.erros(erros)
				.build();
	}

	private Lancamento criarLancamentoAPartirCsv(CSVRecord csvRecord, Usuario usuario) {
		Lancamento lancamento = new Lancamento();
		lancamento.setUsuario(usuario);

		try {
			lancamento.setDescricao(csvRecord.get("descricao"));
			lancamento.setValor(new BigDecimal(csvRecord.get("valor")));
			lancamento.setMes(Integer.parseInt(csvRecord.get("mes")));
			lancamento.setAno(Integer.parseInt(csvRecord.get("ano")));

			String tipoStr = csvRecord.get("tipo");
			if (tipoStr != null && !tipoStr.trim().isEmpty()) {
				lancamento.setTipo(TipoLancamento.valueOf(tipoStr.toUpperCase()));
			}

			String statusStr = csvRecord.get("status");
			if (statusStr != null && !statusStr.trim().isEmpty()) {
				lancamento.setStatus(StatusLancamento.valueOf(statusStr.toUpperCase()));
			} else {
				lancamento.setStatus(StatusLancamento.PENDENTE);
			}

		} catch (IllegalArgumentException e) {
			throw new RegraNegocioException("Valor inválido no arquivo: " + e.getMessage());
		} catch (Exception e) {
			throw new RegraNegocioException("Erro ao processar os dados do arquivo: " + e.getMessage());
		}

		return lancamento;
	}

}
