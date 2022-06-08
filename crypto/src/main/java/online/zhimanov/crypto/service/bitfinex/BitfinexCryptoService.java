package online.zhimanov.crypto.service.bitfinex;

import online.zhimanov.crypto.controller.ws.Message;
import online.zhimanov.crypto.service.CryptoService;
import org.slf4j.event.Level;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;

import java.net.URI;
import java.time.Duration;

public class BitfinexCryptoService implements CryptoService {

    private final DirectProcessor<Message<T>> stream = DirectProcessor.create();

    public BitfinexCryptoService(){
        new ReactorNettyWebSocketClient()
        .execute(URI.create("wss:api.bitfinex.com/ws/2"),new BitfinexWebSocketHandler(stream.sink()))
                .log("Connected to Bitfinex", Level.INFO, SignalType.ON_SUBSCRIBE)
                .retryWhen(e->e.zipWith(Flux.range(0, Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .subscribe();
    }
    @Override
    public Flux<Message<?>> steram(){
        return stream;
    }
}
