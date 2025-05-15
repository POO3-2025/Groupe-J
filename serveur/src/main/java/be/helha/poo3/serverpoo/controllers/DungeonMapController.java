package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Room;
import be.helha.poo3.serverpoo.models.RoomDTO;
import be.helha.poo3.serverpoo.services.DungeonMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/dungeon")
public class DungeonMapController {

    private final DungeonMapService mapService;

    public DungeonMapController(DungeonMapService mapService) {
        this.mapService = mapService;
    }

    @GetMapping("/start")
    public ResponseEntity<RoomDTO> getStartRoom() {
        Room startRoom = mapService.getStartRoom();
        if (startRoom == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RoomDTO(startRoom));
    }

    @GetMapping("/rooms")
    public Collection<RoomDTO> getAllRooms() {
        return mapService.getAllRooms().stream()
                .map(RoomDTO::new)
                .toList(); // Java 16+ ou utiliser .collect(Collectors.toList()) pour compatibilité
    }

    @GetMapping("/room/{id}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable String id) {
        Room room = mapService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RoomDTO(room));
    }
}

