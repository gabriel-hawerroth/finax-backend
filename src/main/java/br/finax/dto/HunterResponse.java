package br.finax.dto;

public record HunterResponse(Data data) {
    public record Data(String result) {
    }
}
