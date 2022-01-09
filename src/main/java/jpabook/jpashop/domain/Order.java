package jpabook.jpashop.domain;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.FetchType.*;


@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    /**
     * em.find() 를 통해 하나만 조회할 때는 FetchType을 EAGER로 해도 하나만 조회하지만
     * JPQL select o From order o; -> 이런식으로 쿼리를 날리면 이 오더에 있는 멤버에대한
     * 정보를 모두 가져온다.. 그래서 N+1문제가 발생한다.
     * 아니면 이 오더에 대해서 멤버 정보를 얻고 싶은 경우에는 fetch join이나 엔티티그래프를 사용하면된다.
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * CascadeType.ALL
     * ex) persist(orderItemA), persist(orderItemC), persist(orderItemC)
     * persist(order) 이렇게 넣어야 하지만
     * 위의 조건을 넣으면 persist(order)만 하면된다.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문 상태 [ORDER, CANCEL]

    /**
     * 양방향 연관관계 세팅 할때 양쪽에 값을 세팅해줘야한다.
     * ex) member를 세팅할때, 멤버를 넣고 멤버쪽에도 Order를 넣어줘야한다.
     * 그래서 두 메서드를 하나로 묶어서 Order에서 Member를 넣으면서
     * Member쪽의 orders에도 order를 넣어준다.
     * 이 메서드의 위치는 핵심적으로 컨트롤을 하는 쪽에 넣는 것이 낫다.
     */
    //==연관관계 메서드==//
    public void setMember(Member member) {
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//

    /**
     * 주문 취소
     */
    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //==조회 로직==//

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        return orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }

}
