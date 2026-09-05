package dev.samster.granthalay.catalog;

public record EditionPriceResponse(String currency, long amountInCents, String territory) {
}
