-- Migration from 054 to 055

UPDATE sample_types SET generated_from_depth = 1 WHERE generated_from_depth > 1;