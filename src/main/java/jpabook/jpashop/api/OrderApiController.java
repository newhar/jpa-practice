package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    // 엔티티 사용 api (절대 사용 금지)
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());
        }
        return all;
    }

    // 엔티티 -> dto 사용 api, 내부적으로 조회되는 엔티티도 모두 dto 로 변환되어야한다.
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = all.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    // 컬렉션 조회시 jpql 을 사용하여 쿼리를 최적화 한다.
    // distinct 키워드를 사용해서 컬렉션 조회시 중복 id 를 제거할 수 있다.
    // 컬렉션 페치조인(일대다)은 1개의 컬렉션에 대해서만 사용해야한다.
    // 단점으로는 페이징을 할 수 없다.
    // jqpl 페치조인과 페이징을 함께 사용하면 db 에서 처리할 수 없기 때문에 (db 와 객체의 차이 때문) 메모리에서 처리하는데 메모리가 터질 위험이 있다.
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> all = orderRepository.findAllwithItem();
        List<OrderDto> collect = all.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    // 페이징과 컬렉션 엔티티를 함께 사용하는 법
    // 1. ToOne(일대일,다대일) 관계는 모두 페치조인을 사용한다.
    // 2. 컬렉션은 LAZYLOADING 을 활용한다.
    // 3. 지연로딩 성능 최적화를 위하여 globalBatchSize 를 설정하여 "IN" query 를 더한다.
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> OrdersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                        @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return result;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> OrderV4() {

        List<OrderQueryDto> result = orderQueryRepository.findOrderQueryDtos();

        return result;
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> OrderV5_Dto_Optimizatino() {
        return orderQueryRepository.findOrderQueryDtos_optimazation();
    }

//    @GetMapping("/api/v6/orders")
//    public List<OrderQueryDto> OrderV5_Dto_Flat() {
//        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
//        return flats.stream()
//                .collect(Collectors.groupingBy(o -> new OrderQueryDto(o.getOrderId(),
//                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
//                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
//                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
//                )).entrySet().stream()
//                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
//                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),   e.getKey().getAddress(), e.getValue()))
//                .collect(toList());
//    }

   @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
//            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
