package com.dsousa.minhasfinancas.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LancamentoCsvResponseDTO {
    private int totalLinhasProcessadas;
    private int totalLinhasComErro;
    private List<ErroProcessamentoCsv> erros;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErroProcessamentoCsv {
        private int linha;
        private List<String> mensagensErro;
    }
}
