
alter table public.recipe_binary drop COLUMN binary_description;

alter table public.recipe_binary add COLUMN binary_description binary(10000000);

