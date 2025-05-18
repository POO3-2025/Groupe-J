/* ================================================================
 * 0.  Sécurité : désactiver puis ré‑activer les FK le temps du DROP
 * ----------------------------------------------------------------*/
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `character`;
DROP TABLE IF EXISTS `user`;
SET FOREIGN_KEY_CHECKS = 1;

/* ================================================================
 * 1.  Table  `user`
 * ----------------------------------------------------------------*/
CREATE TABLE `user` (                        -- « user » est un mot réservé → on le protège avec des backticks
    `id_user`        INT AUTO_INCREMENT PRIMARY KEY,
    `username`      VARCHAR(30)  NOT NULL UNIQUE,
    `password`	  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `role` varchar(10) NOT NULL,
    `activated` tinyint(1) NOT NULL,
    `idLastCharacter` INT NULL
) ENGINE=InnoDB;

/* ================================================================
 * 2.  Table  `character`
 * ----------------------------------------------------------------
 * Si le mot clé vous gêne, remplacez‑le par `game_character`
 * (dans ce cas, adaptez aussi les deux FKs plus bas).
 * ----------------------------------------------------------------*/
CREATE TABLE `character` (
    idCharacter  INT AUTO_INCREMENT PRIMARY KEY,
    idUser       INT          NOT NULL,
    name         VARCHAR(100) NOT NULL,
    inventoryId  VARCHAR(36),
    maxHP        INT          NOT NULL,
    currentHP    INT          NOT NULL,
    constitution INT          NOT NULL,
    dexterity    INT          NOT NULL,
    strength     INT          NOT NULL,

    CONSTRAINT fk_character_user
        FOREIGN KEY (idUser)
        REFERENCES `user`(id_user)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE=InnoDB;