package com.dsousa.minhasfinancas.service;

import com.dsousa.minhasfinancas.model.entity.Usuario;

import java.util.Optional;

public interface UsuarioService {

	Usuario autenticar(String email, String senha);

	Usuario salvarUsuario(Usuario usuario);

	Optional<Usuario> obterPorId(Long id);

}
