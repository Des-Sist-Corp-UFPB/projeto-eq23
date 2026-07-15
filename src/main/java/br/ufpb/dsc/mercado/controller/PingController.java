package br.ufpb.dsc.mercado.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Controller responsável pelo endpoint de healthcheck /ping.
 * Exigido para monitoramento do painel de equipes.
 */
@RestController
public class PingController {

    private final JdbcTemplate jdbcTemplate;

    public PingController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        boolean bancoNoAr;
        try {
            // Select vazio (sem tabela) apenas para validar que a conexão com o banco responde
            jdbcTemplate.execute("SELECT 1");
            bancoNoAr = true;
        } catch (Exception e) {
            bancoNoAr = false;
        }

        Map<String, String> body = Map.of(
            "status", bancoNoAr ? "ok" : "down",
            "service", "eq23",
            "database", bancoNoAr ? "ok" : "down",
            "timestamp", Instant.now().toString()
        );

        return bancoNoAr
            ? ResponseEntity.ok(body)
            : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
