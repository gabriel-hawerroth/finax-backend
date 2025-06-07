package br.finax.dto;

public record AppErrorLog(
        String message,
        String stackTrace
) {
}
