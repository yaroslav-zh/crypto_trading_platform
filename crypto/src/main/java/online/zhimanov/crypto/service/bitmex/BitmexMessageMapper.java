package online.zhimanov.crypto.service.bitmex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import online.zhimanov.crypto.controller.ws.Message;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;

public class BitmexMessageMapper {

    public static List <Message<?>> bitmexToMessage(String payload) {
        try{
            return payload.contains("Sell") || payload.contains("Buy") ?
                    new ObjectMapper().<BitmexMessage<BitmexMessage.InternalTrade>>readValue(
                            payload,
                            new TypeReference<BitmexMessage<BitmexMessage.InternalTrade>>() {
                              })
                            .getData()
                            .stream()
                            .map(d-> Message.trade(d.getTimestamp()
                                    .toInstant()
                                    .toEpochMilli(),
                                    d.getPrice(),
                            d.getHomeNotional(),
                                    "BTC",
                                    "Bitmex"))
                            .collect(Collectors.toList());
        }
        catch (IOException e){
            throw  new UncheckedIOException(e);
        }
    }
}
