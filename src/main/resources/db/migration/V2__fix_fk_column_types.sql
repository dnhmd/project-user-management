alter table users
alter column role_id type bigint;

alter table refresh_tokens
alter column user_id type bigint;