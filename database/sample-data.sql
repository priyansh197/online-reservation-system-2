USE online_reservation;
INSERT INTO trains (train_number, train_name, source_station, destination_station, active) VALUES
 (12951, 'Mumbai Rajdhani',      'Mumbai Central', 'New Delhi',     TRUE),
 (12301, 'Howrah Rajdhani',      'Howrah',         'New Delhi',     TRUE),
 (12627, 'Karnataka Express',    'Bengaluru',      'New Delhi',     TRUE),
 (12009, 'Shatabdi Express',     'Mumbai Central', 'Ahmedabad',     TRUE),
 (11057, 'Amritsar Express',     'Mumbai CST',     'Amritsar',      TRUE),
 (12723, 'Telangana Express',    'Hyderabad',      'New Delhi',     TRUE),
 (19019, 'Dehradun Express',     'Mumbai Bandra',  'Dehradun',      FALSE)
ON DUPLICATE KEY UPDATE train_name = VALUES(train_name);
