package fr.payetonkawa.orders.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "status", nullable = false)
    private String status;

    @PrePersist
    private void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.status = "PENDING"; // Default status when order is created
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void setItems(List<OrderItem> orderItems) {
        this.items.clear();
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                item.setOrder(this);
                this.items.add(item);
            }
        }
    }
}
