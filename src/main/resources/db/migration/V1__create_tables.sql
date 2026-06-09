create table if not exists roles (
    id bigserial primary key,
    name varchar(20) unique not null,
    created_at timestamp default current_timestamp
);

create table if not exists users (
    id bigserial primary key,
    name varchar(50) not null,
    email varchar(120) unique not null,
    hashed_password text,
    created_at timestamp default current_timestamp,
    is_active boolean default true not null,
    role_id int references roles(id) not null,
    password_reset_token text,
    password_reset_token_expires_at timestamp
);

create table if not exists refresh_tokens (
    id bigserial primary key,
    token text,
    user_id int references users(id) not null,
    created_at timestamp default current_timestamp,
    expires_at timestamp not null,
    is_revoked boolean default false not null
);