package online.zhimanov.crypto.service;

import online.zhimanov.crypto.controller.ws.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CryptoService {
    
    Flux<Message<?>> steram();
    
    default Mono<Void> trade(Flux<Message<Message.Trade>> trades, WalletService walletService) {
        return  Mono.empty();
    }
       
}
