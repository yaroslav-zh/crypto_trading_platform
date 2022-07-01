package online.zhimanov.crypto.service.local;

import online.zhimanov.crypto.controller.ws.Message;

public class LocalMessageMapper {
    public static Message<Message.Trade> tradeToMessage(Trade trade) {
        return Message.trade(
                trade.getTimestamp(),
                trade.getPrice(),
                trade.getAmount(),
                trade.getCurrency(),
                trade getMarket()

        );
    }

    public static Trade messageToTrade(Message<Message.Trade> tradeOffer) {
        return new Trade(
                tradeOffer.getTimestamp(),
                tradeOffer.getData().getAmount(),
                tradeOffer.getData().getPrice(),
                tradeOffer.getCurrency(),
                "Local"
        );
    }

    public static Message<Float> walletToMessage(Wallet wallet) {
        return Message.wallet(wallet.getBalance(), wallet.getCurrency(), "Local");
    }
}
