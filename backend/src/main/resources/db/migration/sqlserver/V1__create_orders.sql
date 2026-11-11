create table orders (
    id uniqueidentifier primary key,
    code nvarchar(24) not null,
    customer_name nvarchar(120) not null,
    origin_cep nvarchar(8) not null,
    destination_cep nvarchar(8) not null,
    weight_kg decimal(10, 2) not null,
    status nvarchar(16) not null,
    freight_cents int null,
    carrier nvarchar(80) null,
    estimated_days int null,
    created_at datetime2 not null,
    updated_at datetime2 not null,
    constraint uk_orders_code unique (code)
);

create index ix_orders_status on orders (status);
create index ix_orders_created_at on orders (created_at);
