package vr.mini_autorizador.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.CardResponse;
import vr.mini_autorizador.application.usecase.CardUseCase;

import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/cards")
public class CardController {

    @Autowired
    private CardUseCase useCase;

    @PostMapping
    public ResponseEntity<CardResponse> create(@RequestBody CardRequest request) {
        CardResponse cardResponse = useCase.create(request);
        return ResponseEntity.status(201).body(cardResponse);
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String cardNumber) {
        BigDecimal balance = useCase.getBalance(cardNumber);
        return ResponseEntity.ok(balance);
    }
}
