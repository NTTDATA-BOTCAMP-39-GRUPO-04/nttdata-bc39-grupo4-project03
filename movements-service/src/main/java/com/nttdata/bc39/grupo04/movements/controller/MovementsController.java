package com.nttdata.bc39.grupo04.movements.controller;

import com.nttdata.bc39.grupo04.api.movements.MovementsDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.movements.MovementsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/movements")
public class MovementsController {

    @Autowired
    private MovementsService service;


    @GetMapping("/{number}")
    Flux<MovementsReportDTO> getAllMovementsByNumber(@PathVariable("number") String number) {
        return service.getAllMovementsByNumberAccount(number);
    }

    @PostMapping("/deposit")
    Mono<MovementsDTO> saveDepositMovement(@RequestBody MovementsDTO body) {
        return service.saveDepositMovement(body);
    }

    @PostMapping("/withdrawl")
    Mono<MovementsDTO> saveWithdrawlMovement(@RequestBody MovementsDTO body) {
        return service.saveWithdrawlMovement(body);
    }

    @GetMapping("/all")
    Flux<MovementsReportDTO> getAllMovements() {
        return service.getAllMovements();
    }
}
