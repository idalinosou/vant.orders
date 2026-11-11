package br.com.vant.orders.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("""
            select o from Order o
            where (:status is null or o.status = :status)
              and (:term is null or lower(o.customerName) like lower(concat('%', :term, '%'))
                   or lower(o.code) like lower(concat('%', :term, '%')))
            order by o.createdAt desc
            """)
    List<Order> search(@Param("status") OrderStatus status, @Param("term") String term);

    @Query("select o.status, count(o) from Order o group by o.status")
    List<Object[]> countByStatus();
}
