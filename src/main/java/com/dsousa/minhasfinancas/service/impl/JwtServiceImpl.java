package com.dsousa.minhasfinancas.service.impl;

import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class JwtServiceImpl implements JwtService {

	@Value("${jwt.expiracao::2400}")
	private String expiracao;

	@Value("${jwt.chave-assinatura::test}")
	private String chaveAssinatura;
	
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(chaveAssinatura.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String gerarToken(Usuario usuario) {
		long exp = Long.parseLong(expiracao);
		LocalDateTime dataHoraExpiracao = LocalDateTime.now().plusMinutes(exp);
		Instant instant = dataHoraExpiracao.atZone(ZoneId.systemDefault()).toInstant();
		java.util.Date data = Date.from(instant);
		String horaExpiracaoToken = dataHoraExpiracao.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
		
		return Jwts.builder()
			.setExpiration(data)
			.setSubject(usuario.getEmail())
			.claim("userid", usuario.getId())
			.claim("nome", usuario.getNome())
			.claim("horaExpiracao", horaExpiracaoToken)
			.signWith(getSigningKey())
			.compact();
	}

	@Override
	public Claims obterClaims(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	@Override
	public boolean isTokenValido(String token) {
		try {
			Claims claims = obterClaims(token);
			Date dataExpiracao = claims.getExpiration();
			LocalDateTime data = dataExpiracao.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			return !LocalDateTime.now().isAfter(data);
		} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String obterLoginUsuario(String token) {
		Claims claims = obterClaims(token);
		return claims.getSubject();
	}

}
