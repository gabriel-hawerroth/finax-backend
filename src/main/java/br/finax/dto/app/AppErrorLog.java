package br.finax.dto.app;

public record AppErrorLog(
        String message,
        String stackTrace
) {
}
