alter table image_zoom_levels add column ds text;
update image_zoom_levels set ds = physical_dataset_perm_id;
alter table image_zoom_levels alter column ds set not null;
alter table image_zoom_levels drop column physical_dataset_perm_id;
alter table image_zoom_levels rename ds to physical_dataset_perm_id;
create index image_zoom_levels_phys_ds_idx on image_zoom_levels (physical_dataset_perm_id);
