package com.dsousa.minhasfinancas.service;

import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;
import com.dsousa.minhasfinancas.model.enums.TipoLancamento;
import com.dsousa.minhasfinancas.model.repository.LancamentoRepository;
import com.dsousa.minhasfinancas.model.repository.LancamentoRepositoryTest;
import com.dsousa.minhasfinancas.service.impl.LancamentoServiceImpl;
import lombok.var;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LancamentoServiceTest {

	@SpyBean
	LancamentoServiceImpl service;

	@MockBean
	LancamentoRepository repository;

	@MockBean
	UsuarioService usuarioService;

	@Test
	public void deveSalvarUmLancamento() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		doNothing().when(service).validar(lancamentoASalvar);
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		Lancamento lancamento = service.salvar(lancamentoASalvar);
		assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		assertThat(lancamento.getStatus()).isEqualTo(StatusLancamento.PENDENTE);
	}

	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		Lancamento lancamentoASalvar = LancamentoRepositoryTest.criarLancamento();
		doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		verify(repository, never()).save(lancamentoASalvar);
	}

	@Test
	public void deveAtualizarUmLancamento() {
		Lancamento lancamentoSalvo = LancamentoRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1L);
		lancamentoSalvo.setStatus(StatusLancamento.PENDENTE);
		doNothing().when(service).validar(lancamentoSalvo);
		when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		service.atualizar(lancamentoSalvo);
		verify(repository, times(1)).save(lancamentoSalvo);
	}

	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		verify(repository, never()).save(lancamento);
	}

	@Test
	public void deveDeletarUmLancamento() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		service.deletar(lancamento);
		verify(repository).delete(lancamento);
	}

	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		verify(repository, never()).delete(lancamento);
	}

	@Test
	public void deveFiltrarLancamentos() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		List<Lancamento> lista = Collections.singletonList(lancamento);
		when(repository.findAll(any(Example.class))).thenReturn(lista);
		List<Lancamento> resultado = service.buscar(lancamento);
		assertThat(resultado).isNotEmpty().hasSize(1).contains(lancamento);
	}

	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(1L);
		lancamento.setStatus(StatusLancamento.PENDENTE);
		StatusLancamento novoStatus = StatusLancamento.EFETIVADO;
		doNothing().when(service).atualizar(lancamento);
		service.atualizarStatus(lancamento, novoStatus);
		assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		verify(service).atualizar(lancamento);
	}

	@Test
	public void deveObterUmLancamentoPorID() {
		Long id = 1L;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		Optional<Lancamento> resultado = service.obterPorId(id);
		assertThat(resultado.isPresent()).isTrue();
	}

	@Test
	public void deveREtornarVazioQuandoOLancamentoNaoExiste() {
		Long id = 1L;
		Lancamento lancamento = LancamentoRepositoryTest.criarLancamento();
		lancamento.setId(id);
		when(repository.findById(id)).thenReturn(Optional.empty());
		Optional<Lancamento> resultado = service.obterPorId(id);
		assertThat(resultado.isPresent()).isFalse();
	}

	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		Lancamento lancamento = new Lancamento();

		Throwable erro = Assertions.catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		lancamento.setDescricao("");

		erro = Assertions.catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		lancamento.setDescricao("Salario");

		erro = Assertions.catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		lancamento.setAno(0);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		lancamento.setAno(13);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		lancamento.setMes(1);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		lancamento.setAno(202);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		lancamento.setAno(2020);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		lancamento.setUsuario(new Usuario());

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		lancamento.getUsuario().setId(1L);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		lancamento.setValor(BigDecimal.ZERO);

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		lancamento.setValor(BigDecimal.valueOf(1));

		erro = catchThrowable(() -> service.validar(lancamento));
		assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Tipo de Lançamento.");
	}

	@Test
	public void deveObterSaldoPorUsuario() {
		Long idUsuario = 1L;
		when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(idUsuario, TipoLancamento.RECEITA,
				StatusLancamento.EFETIVADO))
			.thenReturn(BigDecimal.valueOf(100));
		when(repository.obterSaldoPorTipoLancamentoEUsuarioEStatus(idUsuario, TipoLancamento.DESPESA,
				StatusLancamento.EFETIVADO))
			.thenReturn(BigDecimal.valueOf(50));
		BigDecimal saldo = service.obterSaldoPorUsuario(idUsuario);
		assertThat(saldo).isEqualTo(BigDecimal.valueOf(50));
	}

	@Test
	public void deveProcessarArquivoCsvComSucesso() throws IOException {
		// Arrange
		Long usuarioId = 1L;
		String csvContent = "descricao,valor,mes,ano,tipo,status\n" +
				"\"Salário\",5000.00,6,2023,RECEITA,EFETIVADO\n" +
				"\"Aluguel\",1500.00,6,2023,DESPESA,EFETIVADO";

		MultipartFile file = new MockMultipartFile(
				"lancamentos.csv",
				"lancamentos.csv",
				"text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8)
		);

		Usuario usuario = new Usuario();
		usuario.setId(usuarioId);

		when(usuarioService.obterPorId(usuarioId)).thenReturn(Optional.of(usuario));
		when(repository.save(any(Lancamento.class))).thenAnswer(invocation -> {
			Lancamento lancamento = invocation.getArgument(0);
			lancamento.setId(1L);
			return lancamento;
		});

		// Act
		var resultado = service.processarCsv(file, usuarioId);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getTotalLinhasProcessadas()).isEqualTo(2);
		assertThat(resultado.getTotalLinhasComErro()).isZero();
		assertThat(resultado.getErros()).isEmpty();
		verify(repository, times(1)).saveAll(any());
	}

	@Test
	public void deveReportarErrosAoProcessarCsvComDadosInvalidos() throws IOException {
		// Arrange
		Long usuarioId = 1L;
		String csvContent = "descricao,valor,mes,ano,tipo,status\n" +
				"\"\",5000.00,13,2023,RECEITA,EFETIVADO\n" + // Descrição vazia e mês inválido
				"\"Aluguel\",-100.00,6,2023,DESPESA,INVALIDO"; // Valor negativo e status inválido

		MultipartFile file = new MockMultipartFile(
				"lancamentos_invalidos.csv",
				"lancamentos_invalidos.csv",
				"text/csv",
				csvContent.getBytes(StandardCharsets.UTF_8)
		);

		Usuario usuario = new Usuario();
		usuario.setId(usuarioId);

		when(usuarioService.obterPorId(usuarioId)).thenReturn(Optional.of(usuario));

		// Act
		var resultado = service.processarCsv(file, usuarioId);

		// Assert
		assertThat(resultado).isNotNull();
		assertThat(resultado.getTotalLinhasProcessadas()).isEqualTo(2);
		assertThat(resultado.getTotalLinhasComErro()).isEqualTo(2);
		assertThat(resultado.getErros()).hasSize(2);
		verify(repository, never()).save(any(Lancamento.class));

		// Verifica os erros da primeira linha
		var erroLinha1 = resultado.getErros().get(0);
		assertThat(erroLinha1.getLinha()).isEqualTo(2);
		assertThat(erroLinha1.getMensagensErro())
				.anyMatch(msg -> msg.contains("Informe uma Descrição válida"));

		// Verifica os erros da segunda linha
		var erroLinha2 = resultado.getErros().get(1);
		assertThat(erroLinha2.getLinha()).isEqualTo(3);
		assertThat(erroLinha2.getMensagensErro())
				.anyMatch(msg -> msg.contains("Valor inválido no arquivo"));
	}

	@Test
	public void deveLancarExcecaoQuandoArquivoNaoForCsv() {
		// Arrange
		Long usuarioId = 1L;
		String textContent = "Este não é um arquivo CSV";

		Usuario usuario = new Usuario();
		usuario.setId(usuarioId);

		when(usuarioService.obterPorId(usuarioId)).thenReturn(Optional.of(usuario));

		MultipartFile file = new MockMultipartFile(
				"arquivo.txt",
				"arquivo.txt",
				"text/plain",
				textContent.getBytes(StandardCharsets.UTF_8)
		);

		// Act & Assert
		assertThatThrownBy(() -> service.processarCsv(file, usuarioId))
				.isInstanceOf(RegraNegocioException.class)
				.hasMessageContaining("O arquivo deve ser do tipo CSV");
	}
}
