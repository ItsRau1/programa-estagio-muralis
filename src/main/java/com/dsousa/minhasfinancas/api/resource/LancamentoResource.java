package com.dsousa.minhasfinancas.api.resource;

import com.dsousa.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.dsousa.minhasfinancas.api.dto.LancamentoDTO;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Lancamento;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.enums.StatusLancamento;
import com.dsousa.minhasfinancas.model.enums.TipoLancamento;
import com.dsousa.minhasfinancas.service.LancamentoService;
import com.dsousa.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoResource {

	private final LancamentoService service;

	private final UsuarioService usuarioService;
	
	@GetMapping
	public ResponseEntity<?> buscar(
			@RequestParam(value ="descricao" , required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario
			) {
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if(!usuario.isPresent()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não foi possível realizar a consulta. Usuário não encontrado para o Id informado.");
		}
		lancamentoFiltro.setUsuario(usuario.get());
		return ResponseEntity.status(HttpStatus.OK).body(service.buscar(lancamentoFiltro));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> obterLancamento(@PathVariable("id") Long id) {
		return service.obterPorId(id)
					.map(lancamento -> ResponseEntity.status(HttpStatus.OK).body(converter(lancamento)))
					.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@PostMapping
	public ResponseEntity<?> salvar( @RequestBody LancamentoDTO dto ) {
		try {
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return ResponseEntity.status(HttpStatus.CREATED).body(entidade);
		}catch (RegraNegocioException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<?> atualizar(@PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
		return service.obterPorId(id).map( entity -> {
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				return ResponseEntity.status(HttpStatus.OK).body(lancamento);
			}catch (RegraNegocioException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
			}
		}).orElseGet( () -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lancamento não encontrado na base de Dados."));
	}
	
	@PutMapping("/{id}/atualiza-status")
	public ResponseEntity<?> atualizarStatus(@PathVariable("id") Long id , @RequestBody AtualizaStatusDTO dto) {
		return service.obterPorId(id).map( entity -> {
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());

			try {
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.status(HttpStatus.OK).body(entity);
			}catch (RegraNegocioException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
			}
		
		}).orElseGet( () ->
		ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lancamento não encontrado na base de Dados."));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deletar(@PathVariable("id") Long id) {
		return service.obterPorId(id).map( entidade -> {
			service.deletar(entidade);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		}).orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lancamento não encontrado na base de Dados."));
	}
	
	private LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder()
					.id(lancamento.getId())
					.descricao(lancamento.getDescricao())
					.valor(lancamento.getValor())
					.mes(lancamento.getMes())
					.ano(lancamento.getAno())
					.status(lancamento.getStatus().name())
					.tipo(lancamento.getTipo().name())
					.usuario(lancamento.getUsuario().getId())
					.build();
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		Usuario usuario = usuarioService
			.obterPorId(dto.getUsuario())
			.orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado.") );
		lancamento.setUsuario(usuario);
		if(dto.getTipo() != null) {
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		if(dto.getStatus() != null) {
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}
		return lancamento;
	}

}
