
package br.com.boticario.project.pmp.infrastructure.batch.dto;

import java.math.BigDecimal;

public class ProductJsonDTO {

    private String product;
    private Integer quantity;
    private String price; // Vem como String no JSON (ex: "$0.67")
    private String type;
    private String industry;
    private String origin;

    public ProductJsonDTO() {
    }

    public ProductJsonDTO(String product, Integer quantity, String price, String type, String industry, String origin) {
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
        this.industry = industry;
        this.origin = origin;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
