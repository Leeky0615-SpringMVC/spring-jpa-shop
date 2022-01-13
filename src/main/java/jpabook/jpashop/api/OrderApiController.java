package jpabook.jpashop.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderDto;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;
    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public Result ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) { //영속성 강제 주입
            order.getMember().getName();
            order.getDelivery().getAddress();
            order.getOrderItems().stream().forEach(o -> o.getItem().getName());
        }
        return new Result(all);
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 트랜잭션 안에서 지연 로딩 필요
     */
    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        return new Result(orderRepository.findAll(new OrderSearch())
                .stream()
                .map(order -> new OrderDto(order))
                .collect(toList()));
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - 페이징 시에는 N 부분을 포기해야함
     *   대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경가능
     */
    @GetMapping("/api/v3/orders")
    public Result ordersV3(){
        return new Result(orderQueryService.ordersV3());
    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - ToOne 관계만 우선 모두 페치 조인으로 최적화
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(@RequestParam(value = "offset",defaultValue = "0")int offset,
                                @RequestParam(value = "limit",defaultValue = "100")int limit){
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset,limit);
        List<OrderDto> collect = orders.stream().map(OrderDto::new).collect(toList());
        return new Result(collect);
    }

    @GetMapping("/api/v4/orders")
    public Result ordersV4(){
        List<OrderQueryDto> orders = orderQueryRepository.findOrderQueryDtos();
        return new Result(orders);
    }

    @GetMapping("/api/v5/orders")
    public Result ordersV5(){
        List<OrderQueryDto> orders = orderQueryRepository.findAllByDto_Optimization();
        return new Result(orders);
    }

    @GetMapping("/api/v6/orders")
    public Result ordersV6(){
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        List<OrderQueryDto> collect = flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
        return new Result(collect);
    }
}