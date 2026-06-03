package br.ufpb.dsc.mercado.controller;

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

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of(
            "status", "ok",
            "service", "eq23",
            "timestamp", Instant.now().toString()
        );
    }
}
