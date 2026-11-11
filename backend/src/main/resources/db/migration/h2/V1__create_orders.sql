create table orders (
    id uuid primary key,
    code varchar(24) not null,
    customer_name varchar(120) not null,
    origin_cep varchar(8) not null,
    destination_cep varchar(8) not null,
    weight_kg decimal(10, 2) not null,
    status varchar(16) not null,
    freight_cents integer null,
    carrier varchar(80) null,
    estimated_days integer null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_orders_code unique (code)
);

create index ix_orders_status on orders (status);
create index ix_orders_created_at on orders (created_at);
