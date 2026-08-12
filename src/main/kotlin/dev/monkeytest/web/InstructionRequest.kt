package dev.monkeytest.web

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class InstructionRequest(

    @field:NotBlank(message = "instruction must not be blank")
    @field:Pattern(
        regexp = """^(D|G|A\(\d+\)|S\(\d+,\d+,[NSEO]\))$""",
        message = "instruction must match D, G, A(n), or S(x,y,DIR)",
    )
    val instruction: String,
)
