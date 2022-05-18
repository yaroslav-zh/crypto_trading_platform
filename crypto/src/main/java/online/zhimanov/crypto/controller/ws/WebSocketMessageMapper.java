package online.zhimanov.crypto.controller.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.message.Message;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.image.DataBuffer;
import java.util.Collections;

@Service
public class WebSocketMessageMapper {

    private final Jackson2CborEncoder encoder;
    private final Jackson2CborDecoder decoder;


    public WebSocketMessageMapper(ObjectMapper mapper) {
        encoder = new Jackson2CborEncoder(mapper);
        decoder = new Jackson2CborDecoder(mapper);
    }

    public Flux<DataBuffer> encode(Flux<?> outbound, DataBufferFactory dataBufferFactory) {
        return outbound
                .flatMap(i -> encoder.encode(
                        Mono.just(i),
                        dataBufferFactory,
                        ResolvableType.forType(Object.class),
                        MediaType.APPLICATION_JSON,
                        Collections.emptyMap()
                ));
    }


    @SuppressWarnings("unchecked")
    public Flux<Message<Message.Trade>> decode(Flux<DataBuffer> inbound) {
        return inbound.flatMap(p -> decoder.decode(
                        Mono.just(p),
                        ResolvableType.forType(new ParameterizedTypeReference<Message.Trade>>() {}),
                        MediaType.APPLICATION_JSON,
                        Collections.emptyMap()
                ))
                .map(o -> (Message<Message.Trade>) o);
    }
}
