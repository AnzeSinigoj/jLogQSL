CREATE TABLE bands (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    wavelength TEXT
);

CREATE TABLE custom_qths (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    qra_locator TEXT,
    latitude TEXT,
    longitude TEXT
);

CREATE TABLE modes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    description TEXT
);

CREATE TABLE log_owner (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    callsign TEXT NOT NULL,
    name TEXT,
    surname TEXT,
    country TEXT,
    licence_class TEXT,
    notes TEXT
);

CREATE TABLE log_entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    callsign TEXT NOT NULL,
    date TEXT NOT NULL,
    sent_report TEXT NOT NULL,
    received_report TEXT NOT NULL,
    power REAL,
    frequency TEXT,
    qth TEXT,
    note TEXT,
    modes_id INTEGER NOT NULL,
    bands_id INTEGER NOT NULL,
    custom_qths_id INTEGER,
    FOREIGN KEY (modes_id) REFERENCES modes(id),
    FOREIGN KEY (bands_id) REFERENCES bands(id),
    FOREIGN KEY (custom_qths_id) REFERENCES custom_qths(id)
);
