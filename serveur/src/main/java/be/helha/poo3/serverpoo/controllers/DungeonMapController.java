package be.helha.poo3.serverpoo.controllers;

import be.helha.poo3.serverpoo.models.Room;
import be.helha.poo3.serverpoo.models.RoomDTO;
import be.helha.poo3.serverpoo.services.DungeonMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;


/**
 * Contrôleur REST pour la gestion de la carte du donjon.
 * Fournit des points d'accès pour récupérer des informations sur les salles.
 */
@RestController
@RequestMapping("/dungeon")
public class DungeonMapController {

    private final DungeonMapService mapService;

    public DungeonMapController(DungeonMapService mapService) {
        this.mapService = mapService;
    }

    /**
     * Récupère la salle de départ du donjon.
     *
     * @return la salle de départ encapsulée dans un RoomDTO,
     *         ou une réponse 404 si aucune salle de départ n'est définie
     */
    @GetMapping("/start")
    public ResponseEntity<RoomDTO> getStartRoom() {
        Room startRoom = mapService.getStartRoom();
        if (startRoom == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RoomDTO(startRoom));
    }

    /**
     * Récupère toutes les salles du donjon.
     *
     * @return une collection de RoomDTO représentant toutes les salles
     */
    @GetMapping("/rooms")
    public Collection<RoomDTO> getAllRooms() {
        return mapService.getAllRooms().stream()
                .map(RoomDTO::new)
                .toList(); // Java 16+ ou utiliser .collect(Collectors.toList()) pour compatibilité
    }

    /**
     * Récupère une salle spécifique à partir de son identifiant.
     *
     * @param id l'identifiant unique de la salle
     * @return un RoomDTO correspondant à la salle, ou une réponse 404 si non trouvée
     */
    @GetMapping("/room/{id}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable String id) {
        Room room = mapService.getRoomById(id);
        if (room == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(new RoomDTO(room));
    }
}

