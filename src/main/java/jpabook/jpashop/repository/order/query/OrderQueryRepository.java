package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        // 1. toOne
        List<OrderQueryDto> result = findOrders(); // query 1번 -> N 개
        // 2. toMany(collection) for paging
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId()); // query N번
            o.setOrderItems(orderItems);
        });
        return result;
    }

    public List<OrderQueryDto> findOrderQueryDtos_optimazation() {
        // 1. toOne
        List<OrderQueryDto> result = findOrders(); // query 1번

        List<Long> orderIds = result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        // 2. toMany(collection) for paging query 1번
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds",
                OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        // Map 을 사용하여 맵핑 성능 향상 O(1)
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate,\n" +
                        "  o.status, d.address, i.name, oi.orderPrice, oi.count)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i ", OrderFlatDto.class)
                .getResultList();

    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = : orderId",
                OrderItemQueryDto.class
        )
                .setParameter("orderId", orderId)
                .getResultList();

    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();

    }



}
