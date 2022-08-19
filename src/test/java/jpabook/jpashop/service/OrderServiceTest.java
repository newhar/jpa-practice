package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional(readOnly = true)
public class OrderServiceTest {
    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderRepository orderRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ItemRepository itemRepository;
    @Autowired
    OrderService orderService;

    @Test
    @Transactional
    @Rollback(value = false)
    public void 상품주문() {
        // given
        Member member = createMember();
        Item item = createBook();
        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // then
        Order findOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문 시 상태는 오더", OrderStatus.ORDER, findOrder.getStatus());
        assertEquals("total OrderItem count", 1, findOrder.getOrderItems().size());
        assertEquals("price * count", 5000 * orderCount, findOrder.getTotalPrice());
        assertEquals("item stock check", 8, item.getStockQuantity());
    }

    @Test(expected = NotEnoughStockException.class)
    public void 상품주문_재고수량초과() throws Exception {
//Given
        Member member = createMember();
        Item item = createBook(); //이름, 가격, 재고
        int orderCount = 11; //재고보다 많은 수량 //When
        orderService.order(member.getId(), item.getId(), orderCount);
        //Then
        fail("재고 수량 부족 예외가 발생해야 한다."); }

    private Book createBook() {
        Book book = new Book();
        book.setName("spring book");
        book.setAuthor("do no");
        book.setIsbn("05924152");
        book.setPrice(5000);
        book.addStock(10);

        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("jin");
        member.setAddress(new Address("seoul", "jeonnong", "12345"));

        em.persist(member);
        return member;
    }

}