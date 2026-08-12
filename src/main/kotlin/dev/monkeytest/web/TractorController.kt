package dev.monkeytest.web

import dev.monkeytest.service.InstructionParser
import dev.monkeytest.service.TractorService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/instructions")
class TractorController(val tractorService: TractorService, val parser: InstructionParser) {

    @PostMapping
    fun submit(
        @Valid @RequestBody instructionRequest: InstructionRequest
    ): ResponseEntity<Void> {
        parser.parse(instructionRequest.instruction).let { tractorService.executeInstruction(it) }
        return ResponseEntity.accepted().build()
    }
}