package vr.mini_autorizador.infrastructure.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import vr.mini_autorizador.application.dto.TransactionRequest;
import vr.mini_autorizador.application.usecase.TransactionUseCase;

@RestController
@RequestMapping("/transacoes")
public class TransactionController {

    @Autowired
    private TransactionUseCase useCase;

    @PostMapping
    public ResponseEntity<String> process(@RequestBody TransactionRequest request) {
        useCase.process(request);
        return ResponseEntity.status(201).body("OK");
    }
}
