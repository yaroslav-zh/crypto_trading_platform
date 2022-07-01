package online.zhimanov.crypto.service.local;


import lombok.RequiredArgsConstructor;
import online.zhimanov.crypto.controller.ws.Message;
import online.zhimanov.crypto.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalCryptoService {

    private final DirectProcessor<Message<?>> stream =DirectProcessor.create();
    private final TrdRepository traderepository;

    @Override
    public Flux<Messge<?>> stream(){
        return stream;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Mono<Void>trade(Flux<Message<Message.Trade>> tradeOffer, WalletService walletService) {
        return tradeOffer
                .onBackpressureBuffer()
                .flatMap(trade ->
                        walletService.withdraw(trade))
                .then(doTrade(trade))
                .then(walletService.adjust(trade)
                        .then(doStoreTrade(trade))
                        .onErrorResume(Wallet.NotEnoughMoneyExeption.class, t -> Mono.empty())
                        .onErrorResume(t -> walletService.rollback(trade).then(Mono.empty()))
                                )
                .map(LocalMesageMapper::tradeToMessage)
                .doOnNext(stream.sink()::next)
                .then();
    }

    private Mono<Void> doTrade(Message<Message.Trade> trade){
        return Mono.just(trade)
                .delayElement(Duration.ofMillis(TreadLocalRandom.current()
                        .nextInt(2000)))
                .timeout(Duration.ofMillis(1000))
                .then();
    }

    private Mono<Trade> doStoreTrade(Message<Message.Trade> tradeMessage){
        return tradesRepository.save(LocalMessageMapper.messageToTrade(tradeMessage))
                .timeout(Duration.ofSeconds(2))
                .retryWhen(e -> e.zipWith(Flux.range(0, Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .publishOn(Schedulers.parallel());
    }

}
