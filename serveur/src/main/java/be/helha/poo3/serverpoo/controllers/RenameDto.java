package be.helha.poo3.serverpoo.controllers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameDto(
        @NotBlank @Size(max = 30) String newName
) {}
