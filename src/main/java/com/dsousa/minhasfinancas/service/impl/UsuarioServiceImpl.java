package com.dsousa.minhasfinancas.service.impl;

import com.dsousa.minhasfinancas.exception.ErroAutenticacao;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.model.repository.UsuarioRepository;
import com.dsousa.minhasfinancas.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

	private final UsuarioRepository repository;

	private final PasswordEncoder encoder;

	@Override
	public Usuario autenticar(String email, String senha) {
		Usuario usuario = repository.findByEmail(email)
			.orElseThrow(() -> new ErroAutenticacao("Usuário não encontrado para o email informado."));
		this.validarSenha(senha, usuario.getSenha());
		return usuario;
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		usuario.criptografarSenha();
		return repository.save(usuario);
	}

	@Override
	public Optional<Usuario> obterPorId(Long id) {
		return repository.findById(id);
	}

	private void validarEmail(String email) {
		if (repository.existsByEmail(email)) {
			throw new RegraNegocioException("Já existe um usuário cadastrado com este email.");
		}
	}

	private void validarSenha(String senhaEnviada, String senhaUsuario) {
		if (!encoder.matches(senhaEnviada, senhaUsuario)) {
			throw new ErroAutenticacao("Senha inválida.");
		}
	}

}
