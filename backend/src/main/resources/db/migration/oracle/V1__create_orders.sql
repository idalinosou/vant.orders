create table orders (
    id raw(16) primary key,
    code varchar2(24) not null,
    customer_name varchar2(120) not null,
    origin_cep varchar2(8) not null,
    destination_cep varchar2(8) not null,
    weight_kg number(10, 2) not null,
    status varchar2(16) not null,
    freight_cents number(10) null,
    carrier varchar2(80) null,
    estimated_days number(3) null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uk_orders_code unique (code)
);

create index ix_orders_status on orders (status);
create index ix_orders_created_at on orders (created_at);
