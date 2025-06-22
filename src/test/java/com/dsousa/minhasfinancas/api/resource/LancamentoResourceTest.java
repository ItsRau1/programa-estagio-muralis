package com.dsousa.minhasfinancas.api.resource;

import com.dsousa.minhasfinancas.api.dto.LancamentoCsvResponseDTO;
import com.dsousa.minhasfinancas.exception.RegraNegocioException;
import com.dsousa.minhasfinancas.model.entity.Usuario;
import com.dsousa.minhasfinancas.service.LancamentoService;
import com.dsousa.minhasfinancas.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class LancamentoResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LancamentoService lancamentoService;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deveProcessarArquivoCsvComSucesso() throws Exception {
        // Arrange
        String csvContent = "descricao,valor,mes,ano,tipo,status\n" +
                "\"Salário\",5000.00,6,2023,RECEITA,EFETIVADO\n" +
                "\"Aluguel\",1500.00,6,2023,DESPESA,EFETIVADO";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lancamentos.csv",
                "text/csv",
                csvContent.getBytes()
        );

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        LancamentoCsvResponseDTO responseDTO = LancamentoCsvResponseDTO.builder()
                .totalLinhasProcessadas(2)
                .totalLinhasComErro(0)
                .build();

        given(usuarioService.obterPorId(anyLong())).willReturn(Optional.of(usuario));
        given(lancamentoService.processarCsv(any(), anyLong())).willReturn(responseDTO);

        // Act & Assert
        mvc.perform(MockMvcRequestBuilders.multipart("/api/lancamentos/upload-csv")
                        .file(file)
                        .param("usuario", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLinhasProcessadas").value(2))
                .andExpect(jsonPath("$.totalLinhasComErro").value(0));
    }

    @Test
    public void deveRetornarErroQuandoArquivoNaoForEnviado() throws Exception {
        // Act & Assert
        mvc.perform(MockMvcRequestBuilders.multipart("/api/lancamentos/upload-csv")
                        .param("usuario", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void deveRetornarErroQuandoArquivoNaoForCsv() throws Exception {
        // Arrange
        String textContent = "Este não é um arquivo CSV";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "arquivo.txt",
                "text/plain",
                textContent.getBytes()
        );

        // Act & Assert
        mvc.perform(MockMvcRequestBuilders.multipart("/api/lancamentos/upload-csv")
                        .file(file)
                        .param("usuario", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Por favor, envie um arquivo CSV."));
    }

    @Test
    public void deveRetornarErroQuandoUsuarioNaoEncontrado() throws Exception {
        // Arrange
        String csvContent = "descricao,valor,mes,ano,tipo,status\n\"Salário\",5000.00,6,2023,RECEITA,EFETIVADO";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "lancamentos.csv",
                "text/csv",
                csvContent.getBytes()
        );

        given(lancamentoService.processarCsv(any(), anyLong())).willThrow(new RegraNegocioException("Usuário não encontrado."));

        // Act & Assert
        mvc.perform(MockMvcRequestBuilders.multipart("/api/lancamentos/upload-csv")
                        .file(file)
                        .param("usuario", "999")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Usuário não encontrado."));
    }
}
