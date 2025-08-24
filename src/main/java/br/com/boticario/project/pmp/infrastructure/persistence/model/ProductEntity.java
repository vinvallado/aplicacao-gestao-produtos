
package br.com.boticario.project.pmp.infrastructure.persistence.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"product", "type"})
})
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String product;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private String industry;

    @Column(nullable = false)
    private String origin;

    public ProductEntity() {
    }

    public ProductEntity(Long id, String product, String type, BigDecimal price, Integer quantity, String industry, String origin) {
        this.id = id;
        this.product = product;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.industry = industry;
        this.origin = origin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
