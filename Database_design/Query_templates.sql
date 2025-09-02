--Display all entries with text insted of FK
SELECT L.ID AS ID, L.callsign AS CALLSIGN, L.date AS 'DATE & TIME', L.sent_report AS 'SENT REPORT', L.received_report AS 'RECEIVED REPORT', 
       B.name AS BAND, M.name AS MODE, L.power AS POWER, L.frequency AS FREQUENCY, L.qth AS QTH, Q.name AS 'MY QTH', L.note AS NOTE
FROM log_entries L
LEFT JOIN modes M ON M.id = L.modes_id
LEFT JOIN BANDS B ON B.id = L.bands_id
LEFT JOIN custom_qths Q ON Q.id = L.custom_qths_id;

--Callsign search
SELECT L.ID AS ID, L.callsign AS CALLSIGN, L.date AS 'DATE & TIME', L.sent_report AS 'SENT REPORT', L.received_report AS 'RECEIVED REPORT', 
       B.name AS BAND, M.name AS MODE, L.power AS POWER, L.frequency AS FREQUENCY, L.qth AS QTH, Q.name AS 'MY QTH', L.note AS NOTE
FROM log_entries L
LEFT JOIN modes M ON M.id = L.modes_id
LEFT JOIN BANDS B ON B.id = L.bands_id
LEFT JOIN custom_qths Q ON Q.id = L.custom_qths_id
where callsign = '';

--Update log
UPDATE log_entries
SET callsign = ?,
    date = ?,
    sent_report = ?,
    received_report = ?,
    power = ?,
    frequency = ?,
    qth = ?,
    note = ?,
    modes_id = ?,
    bands_id = ?,
    custom_qths_id = ?
WHERE id = ?;

-- Update bands
UPDATE bands
SET
    name = ?,
    wavelength = ?
WHERE id = ?;

-- Update custom_qths


-- Update modes
UPDATE modes
SET
    name = ?,
    description = ?
WHERE id = ?;

-- Update log_owner
UPDATE log_owner
SET
    callsign = ?,
    name = ?,
    surname = ?,
    country = ?,
    licence_class = ?,
    notes = ?
WHERE id = ?;