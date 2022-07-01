package online.zhimanov.crypto.service.local;

import lombok.RequiredArgsConstructor;
import online.zhimanov.crypto.controller.ws.Message;
import online.zhimanov.crypto.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.time.Duration;


@Service
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LocalWalletService implements WalletService {

    private final DirectProcessor<Wallet> stream = DirectProcessor.create();
    private final WalletRepository        walletRepository;

    @Override
    public Flux<Message<Float>> stateStream() {
        return currentUser()
                .flatMapMany(walletRepository::findAllByOwner)
                .timeout(Duration.ofSeconds(2))
                .retryWhen(e -> e.zipWith(Flux.range(0,  Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .mergeWith(stream)
                .map(LocalMessageMapper::walletToMessage);
    }

    @Override
    public Mono<Void> withdraw(Message<Message.Trade> trade) {
        return currentUser()
                .flatMap(owner -> walletRepository.findByOwnerAndCurrency(
                        owner,
                        withdrawCurrency(trade)
                ))
                .map(wallets -> wallets.withdraw(calculateWithdraw(trade)))
                .flatMap(walletRepository::save)
                .timeout(Duration.ofSeconds(2))
                .retryWhen(e -> e.zipWith(Flux.range(0,  Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .doOnNext(stream.sink()::next)
                .then();
    }

    @Override
    public Mono<Void> adjust(Message<Message.Trade> trade) {
        return currentUser()
                .flatMap(owner -> walletRepository.findByOwnerAndCurrency(
                        owner,
                        adjustCurrency(trade)
                ))
                .map(wallets -> wallets.adjust(calculateAdjust(trade)))
                .flatMap(walletRepository::save)
                .timeout(Duration.ofSeconds(2))
                .retryWhen(e -> e.zipWith(Flux.range(0,  Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .doOnNext(stream.sink()::next)
                .then();
    }

    @Override
    public Mono<Void> rollback(Message<Message.Trade> trade) {
        return currentUser()
                .flatMap(owner -> walletRepository.findByOwnerAndCurrency(
                        owner,
                        withdrawCurrency(trade)
                ))
                .map(wallets -> wallets.adjust(calculateWithdraw(trade)))
                .flatMap(walletRepository::save)
                .timeout(Duration.ofSeconds(2))
                .retryWhen(e -> e.zipWith(Flux.range(0,  Integer.MAX_VALUE))
                        .delayElements(Duration.ofMillis(200)))
                .doOnNext(stream.sink()::next)
                .then();
    }

    private static Mono<String> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName);
    }

    private static String withdrawCurrency(Message<Message.Trade> trade) {
        boolean isBuyBitcoin = trade.getData()
                .getAmount() > 0;
        return isBuyBitcoin ? "USD" : trade.getCurrency();
    }

    private static String adjustCurrency(Message<Message.Trade> trade) {
        boolean isBuyBitcoin = trade.getData()
                .getAmount() > 0;
        return isBuyBitcoin ? trade.getCurrency() : "USD";
    }

    private static float calculateWithdraw(Message<Message.Trade> trade) {
        boolean isBuyBitcoin = trade.getData()
                .getAmount() > 0;
        return Math.abs(trade.getData()
                .getAmount()) * (isBuyBitcoin ? trade.getData()
                .getPrice() : 1f);
    }

    private static float calculateAdjust(Message<Message.Trade> trade) {
        boolean isBuyBitcoin = trade.getData()
                .getAmount() > 0;
        return Math.abs(trade.getData()
                .getAmount()) * (isBuyBitcoin ? 1f : trade.getData()
                .getPrice());
    }
}
