-- Ghost Net Fishing: Tabellenstruktur (MySQL 8)
--
-- Hinweis: Hibernate legt diese Tabellen beim ersten Deployment automatisch an
-- (hibernate.hbm2ddl.auto=update in persistence.xml). Dieses Skript dokumentiert
-- die Struktur und kann alternativ manuell ausgeführt werden.

USE ghostnet;

CREATE TABLE IF NOT EXISTS person (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    telefonnummer   VARCHAR(30)  NOT NULL,
    erfasst_am      DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_person_telefonnummer UNIQUE (telefonnummer)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS geisternetz (
    id                           BIGINT       NOT NULL AUTO_INCREMENT,
    breitengrad                  DOUBLE       NOT NULL,
    laengengrad                  DOUBLE       NOT NULL,
    geschaetzte_groesse_qm       INT          NOT NULL,
    bemerkung                    VARCHAR(500) NULL,
    status                       ENUM ('GEMELDET', 'BERGUNG_BEVORSTEHEND', 'GEBORGEN', 'VERSCHOLLEN') NOT NULL,
    gemeldet_am                  DATETIME(6)  NOT NULL,
    status_geaendert_am          DATETIME(6)  NOT NULL,
    meldende_person_id           BIGINT       NULL,  -- NULL = anonyme Meldung
    bergende_person_id           BIGINT       NULL,  -- höchstens eine bergende Person je Netz
    verschollen_gemeldet_von_id  BIGINT       NULL,
    version                      BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_netz_meldende_person    FOREIGN KEY (meldende_person_id)          REFERENCES person (id),
    CONSTRAINT fk_netz_bergende_person    FOREIGN KEY (bergende_person_id)          REFERENCES person (id),
    CONSTRAINT fk_netz_verschollen_person FOREIGN KEY (verschollen_gemeldet_von_id) REFERENCES person (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
