package online.zhimanov.crypto.service.bitmex;

import lombok.RequiredArgsConstructor;
import online.zhimanov.crypto.controller.ws.Message;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class BitmexWebSocketHandler implements WebSocketHandler {

    private final FluxSink<Message<?>> sink;

    @Override
    public Mono<Void> handle(WebSocketSession s) {
        return s.receive()
                .skip(6)
                .map(WebSocketMessage::getPayloadAsText)
                .publishOn(Schedulers.parallel())
                .flatMapIterable(BitmexMessageMapper::bitmexToMessage)
                .doOnNext(sink::next)
                .then();
    }
}
