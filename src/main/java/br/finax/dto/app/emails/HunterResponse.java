package br.finax.dto.app.emails;

public record HunterResponse(Data data) {
    public record Data(String result) {
    }
}
