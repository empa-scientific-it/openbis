-- Migration from 009 to 010

ALTER TABLE acquired_images ADD COLUMN image_transformer_factory BYTEA;