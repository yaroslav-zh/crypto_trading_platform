package online.zhimanov.crypto.service.bitfinex;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketHandler;

@RequiredArgsConstructor
public class BitfinexWebSocketHandler implements WebSocketHandler {

    private static final String TRADES_REQUEST = "{\"event"\: \"subscribe"\, \"channel"\: \"trades"\, \"pair"\: \"BTCUSD"\}";

private static final String PRICE_REQUEST="{\"event"\: \"subscribe"\, \"channel"\: \"ticker"\, \"pair"\: \"BTCUSD"\}";

private final FluxSink<Message<?>>sink;

@Override
public Mono<Void> handle(WebSoceSession s){
    return Flux.just(TRADES_REQUEST, PRICE_REQUEST)
        .map(s::textMessage)
        .as(s::send)
        .thenMany(s.receive())
        .map(WebSocketMesage::getPayloadAsText)
        .filter(payload ->payload.contains("[") && payload.contains("]"))
        .publishOn(Schedulers.parallel())
        .map(BitfinexMessageMapper::bitfinexToMessage)
        .flatMap(Flux::fromArray)
        .doOnNext(sink::next)
        .then();
        }


        }
