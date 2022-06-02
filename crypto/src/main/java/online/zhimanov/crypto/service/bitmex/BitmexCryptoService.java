package online.zhimanov.crypto.service.bitmex;

import online.zhimanov.crypto.controller.ws.Message;
import online.zhimanov.crypto.service.CryptoService;
import org.slf4j.event.Level;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.SignalType;

import java.net.URI;
import java.time.Duration;

public class BitmexCryptoService extends CryptoService {

    private final Flux<Message<?>> stream = Flux.create(BitmexCryptoService:: connect)
            .publish()
            .autoConnect(0);


    @Override
    public Flux<Message<?>> stream(){
        return  stream;
    }

    private static void connect(FluxSink<Message<?>> sink){
        new ReactorNettyWebSocketClient()
                .execute(URI.create("wss://www.bitmex.com/realtime?subscribe=instrument:XBTUSD,trade:XBTUSD"),
                        new BitmexWebSocketHandler(sink)
                )
                .log("Connect to Bitmex", Level.INFO, SignalType.ON_SUBSCRIBE)
                .retry(e->e.zipWith(Flux.range(0, Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .subscribe();

    }
}
