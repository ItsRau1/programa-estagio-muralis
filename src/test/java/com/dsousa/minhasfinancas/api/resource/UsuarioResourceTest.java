package com.dsousa.minhasfinancas.api.resource;

import com.dsousa.minhasfinancas.api.dto.UsuarioDTO;
import com.dsousa.minhasfinancas.exception.ErroAutenticacao;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.service.JwtService;
import com.dsousa.minhasfinancas.service.LancamentoService;
import com.dsousa.minhasfinancas.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioResourceTest {

	static final String API = "/api/usuarios";
	static final MediaType JSON = MediaType.APPLICATION_JSON;

	@Autowired
	MockMvc mvc;

	@MockBean
	UsuarioService service;

	@MockBean
	LancamentoService lancamentoService;

	@MockBean
	JwtService jwtService;

	@Test
	public void deveAutenticarUmUsuario() throws Exception {
		String email = "usuario@email.com";
		String senha = "123";
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		Usuario usuario = Usuario.builder().id(1L).email(email).senha(senha).build();
		Mockito.when(service.autenticar(email, senha)).thenReturn(usuario);
		String json = new ObjectMapper().writeValueAsString(dto);
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(API.concat("/autenticar"))
			.accept(JSON)
			.contentType(JSON)
			.content(json);
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()));
	}

	@Test
	public void deveRetornarBadRequestAoObterErroDeAutenticacao() throws Exception {
		String email = "usuario@email.com";
		String senha = "123";
		UsuarioDTO dto = UsuarioDTO.builder().email(email).senha(senha).build();
		Mockito.when(service.autenticar(email, senha)).thenThrow(ErroAutenticacao.class);
		String json = new ObjectMapper().writeValueAsString(dto);
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(API.concat("/autenticar"))
			.accept(JSON)
			.contentType(JSON)
			.content(json);
		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void deveCriarUmNovoUsuario() throws Exception {
		String email = "usuario@email.com";
		String senha = "123";
		UsuarioDTO dto = UsuarioDTO.builder().email("usuario@email.com").senha("123").build();
		Usuario usuario = Usuario.builder().id(1L).email(email).senha(senha).build();
		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenReturn(usuario);
		String json = new ObjectMapper().writeValueAsString(dto);
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(API)
			.accept(JSON)
			.contentType(JSON)
			.content(json);
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isCreated())
			.andExpect(MockMvcResultMatchers.jsonPath("id").value(usuario.getId()))
			.andExpect(MockMvcResultMatchers.jsonPath("nome").value(usuario.getNome()))
			.andExpect(MockMvcResultMatchers.jsonPath("email").value(usuario.getEmail()));
	}

	@Test
	public void deveRetornarBadRequestAoTentarCriarUmUsuarioInvalido() throws Exception {
		UsuarioDTO dto = UsuarioDTO.builder().email("usuario@email.com").senha("123").build();
		Mockito.when(service.salvarUsuario(Mockito.any(Usuario.class))).thenThrow(RegraNegocioException.class);
		String json = new ObjectMapper().writeValueAsString(dto);
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(API)
			.accept(JSON)
			.contentType(JSON)
			.content(json);
		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void deveObterOSaldoDoUsuario() throws Exception {
		BigDecimal saldo = BigDecimal.valueOf(10);
		Usuario usuario = Usuario.builder().id(1L).email("usuario@email.com").senha("123").build();
		Mockito.when(service.obterPorId(1L)).thenReturn(Optional.of(usuario));
		Mockito.when(lancamentoService.obterSaldoPorUsuario(1L)).thenReturn(saldo);
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(API.concat("/1/saldo"))
			.accept(JSON)
			.contentType(JSON);
		mvc.perform(request)
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.content().string("10"));
	}

	@Test
	public void deveRetornarResourceNotFoundQuandoUsuarioNaoExisteParaObterOSaldo() throws Exception {
		Mockito.when(service.obterPorId(1L)).thenReturn(Optional.empty());
		MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(API.concat("/1/saldo"))
			.accept(JSON)
			.contentType(JSON);
		mvc.perform(request).andExpect(MockMvcResultMatchers.status().isNotFound());
	}

}
