package dev.monkeytest.web

import dev.monkeytest.service.InstructionParser
import dev.monkeytest.service.Position
import dev.monkeytest.service.TractorFleet
import dev.monkeytest.service.TractorId
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Validated
@RestController
@RequestMapping("/tractors")
class TractorController(
    val fleet: TractorFleet,
    val parser: InstructionParser,
    val positionBroadcaster: PositionBroadcaster
) {

    @PostMapping
    fun create(@Valid @RequestBody request: CreateTractorRequest): ResponseEntity<TractorIdResponse> {
        val id = fleet.create(Position(request.x, request.y, request.direction))
        return ResponseEntity.ok(TractorIdResponse(id.value))
    }

    @PostMapping("/{id}/instructions")
    fun submit(
        @PathVariable id: String,
        @Valid @RequestBody instructionRequest: InstructionRequest
    ): ResponseEntity<Void> {
        val tractor = fleet.get(TractorId(id))
        parser.parse(instructionRequest.instruction).let { tractor.executeInstruction(it) }
        return ResponseEntity.accepted().build()
    }

    @GetMapping("/stream")
    fun stream(): SseEmitter =
        positionBroadcaster.subscribe(fleet.all().associate { it.id to it.currentPosition() })
}
