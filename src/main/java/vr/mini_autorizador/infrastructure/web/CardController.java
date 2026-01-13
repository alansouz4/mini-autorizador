package vr.mini_autorizador.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vr.mini_autorizador.application.dto.CardRequest;
import vr.mini_autorizador.application.dto.CardResponse;
import vr.mini_autorizador.application.usecase.CardUseCase;

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
}
