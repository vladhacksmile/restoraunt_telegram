package com.vladhacksmile.crm.gpt;

import lombok.Getter;

@Getter
public enum TelegramEmoji {
    SHOP("\uD83D\uDED2"),
    SHOP_BAG("\uD83D\uDECD"),
    BANK_CARD("\uD83C\uDFE6"),
    DOLLAR("\uD83D\uDCB5"),
    CALENDAR("\uD83D\uDDD3"),
    ACCEPT("✅"),
    WARNING("‼️"),
    CARROT("\uD83E\uDD55"),
    BOOKS("\uD83D\uDCDA"),
    HELLO("\uD83D\uDC4B"),
    ID("\uD83C\uDD94"),
    PIZZA("\uD83C\uDF55"),
    INFO("ℹ️"),
    GRID("#️⃣"),
    WEIGHT("⚖️"),
    PLATE("\uD83E\uDD63");

    private final String symbol;

    TelegramEmoji(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return this.symbol;
    }
}
