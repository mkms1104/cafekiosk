package sample.cafekiosk.spring.api.service.order;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sample.cafekiosk.spring.IntegrationTestSupport;
import sample.cafekiosk.spring.client.mail.MailSendClient;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistory;
import sample.cafekiosk.spring.domain.history.mail.MailSendHistoryRepository;
import sample.cafekiosk.spring.domain.order.Order;
import sample.cafekiosk.spring.domain.order.OrderRepository;
import sample.cafekiosk.spring.domain.order.OrderStatus;
import sample.cafekiosk.spring.domain.orderproduct.OrderProductRepository;
import sample.cafekiosk.spring.domain.product.Product;
import sample.cafekiosk.spring.domain.product.ProductRepository;
import sample.cafekiosk.spring.domain.product.ProductSellingStatus;
import sample.cafekiosk.spring.domain.product.ProductType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static sample.cafekiosk.spring.domain.product.ProductType.*;

class OrderStaticsServiceTest extends IntegrationTestSupport {
    @Autowired
    private OrderStaticsService orderStaticsService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MailSendHistoryRepository mailSendHistoryRepository;

    @AfterEach
    void tearDown() {
        orderProductRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        mailSendHistoryRepository.deleteAllInBatch();
    }

    @DisplayName("결제완료 주문들을 조회하여 매출 통계 메일을 전송한다.")
    @Test
    void sendOrderStaticsMail() {
        // Given
        LocalDateTime now = LocalDateTime.of(2023, 3, 5, 0, 0);

        Product product1 = createProduct(BOTTLE, "001", 1000);
        Product product2 = createProduct(BAKERY, "002", 3000);
        Product product3 = createProduct(HANDMADE, "003", 5000);

        List<Product> products = List.of(product1, product2, product3);
        productRepository.saveAll(products);

        Order order1 = createPaymentComplementOrder(LocalDateTime.of(2023, 3, 4, 23, 59), products);
        Order order2 = createPaymentComplementOrder(now, products);
        Order order3 = createPaymentComplementOrder(LocalDateTime.of(2023, 3, 5, 23, 59), products);
        Order order4 = createPaymentComplementOrder(LocalDateTime.of(2023, 3, 6, 0, 0), products);

        Mockito.when(mailSendClient.sendEmail(any(String.class), any(String.class), any(String.class), any(String.class)))
                .thenReturn(true);

        // When
        boolean result = orderStaticsService.sendOrderStaticsMail(LocalDate.of(2023, 3, 5), "test@test.com");

        // Then
        assertThat(result).isTrue();
        List<MailSendHistory> histories = mailSendHistoryRepository.findAll();
        assertThat(histories).hasSize(1)
                .extracting("content")
                .contains("총 매출 합계는 18000원입니다.");

    }

    private Order createPaymentComplementOrder(LocalDateTime now, List<Product> products) {
        Order order = Order.builder()
                .products(products)
                .orderStatus(OrderStatus.PAYMENT_COMPLETED)
                .registeredDateTime(now)
                .build();

        return orderRepository.save(order);
    }


    private Product createProduct(ProductType type, String productNumber, int price) {
        return Product.builder()
                .type(type)
                .productNumber(productNumber)
                .price(price)
                .sellingStatus(ProductSellingStatus.SELLING)
                .name("메뉴 이름")
                .build();
    }

}