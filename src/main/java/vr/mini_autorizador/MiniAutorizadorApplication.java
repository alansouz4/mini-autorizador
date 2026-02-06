package vr.mini_autorizador;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry(order = Ordered.HIGHEST_PRECEDENCE) // Garante que o retry envolva a transação
public class MiniAutorizadorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniAutorizadorApplication.class, args);
	}

}
