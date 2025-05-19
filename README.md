## Initialisation du projet

Avant de lancer l'application, il est nécessaire de créer la base de données et ses tables.

### Création de la base de données

La base de données doit s'appeler :  
`mail_manager`

### Création des tables

Exécutez les commandes SQL suivantes pour créer les tables nécessaires :

```sql
CREATE TABLE `user` (
  `user_id` VARCHAR(36) NOT NULL,
  `mail` VARCHAR(50) NOT NULL,
  `fullName` VARCHAR(40),
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `oauth_tokens` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(36) NOT NULL,
  `provider` VARCHAR(50) NOT NULL,
  `access_token` TEXT NOT NULL,
  `access_token_expires_at` TIMESTAMP NOT NULL,
  `refresh_token` TEXT,
  `refresh_token_expires_at` TIMESTAMP NULL DEFAULT NULL,
  `token_type` VARCHAR(50) DEFAULT 'Bearer',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_id` (`user_id`),
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user`(`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
