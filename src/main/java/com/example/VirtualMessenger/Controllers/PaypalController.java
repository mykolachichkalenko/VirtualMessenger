package com.example.VirtualMessenger.Controllers;

import com.example.VirtualMessenger.Services.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PaypalController {

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService) {
        this.paypalService = paypalService;
    }

    @PostMapping("/paypal/create")
    public Mono<Map<String, String>> createPayment() {
       return Mono.fromCallable(() -> {
            String cancelUrl = "http://localhost:5173/cancel";
            String successUrl = "http://localhost:5173/success";
            Payment payment = paypalService.createPayment(
                    10.0, "USD", "paypal", "sale", "Virtual Messenger Premium Version", cancelUrl, successUrl);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    Map<String, String> response = new HashMap<>();
                    response.put("approvalUrl", links.getHref());
                    return response;
                }
            }
            return Map.of("error", "error");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/test/pay")
    public Mono<Void> testPaying (Authentication authentication){
        String phone = authentication.getPrincipal().toString();
        return paypalService.testPaying(phone);
    }
}
