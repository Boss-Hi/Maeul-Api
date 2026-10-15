ALTER TABLE itinerary_tours
    ADD COLUMN start_time TIME NULL AFTER sequence,
    ADD COLUMN end_time TIME NULL AFTER start_time;

