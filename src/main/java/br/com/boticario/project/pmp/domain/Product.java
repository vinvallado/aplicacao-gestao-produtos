package br.com.boticario.project.pmp.domain;

import java.math.BigDecimal;

/**
 * Representa a entidade de domínio Produto.
 * Esta é uma classe pura, sem anotações de frameworks (JPA, Jackson, etc).
 * Ela contém os dados e a lógica de negócio que pertencem intrinsecamente a um produto.
 */
public class Product {

    private Long id;
    private String product; // Nome do produto
    private String type;    // Tipo/Categoria do produto
    private BigDecimal price; // Usamos BigDecimal para precisão monetária
    private Integer quantity; // Quantidade em estoque
    private String industry;  // Setor/Indústria do produto
    private String origin;    // Origem/Estado de origem do produto

    // Construtor padrão (NoArgsConstructor)
    public Product() {
    }

    // Construtor com todos os argumentos (AllArgsConstructor)
    public Product(Long id, String product, String type, BigDecimal price, Integer quantity, String industry, String origin) {
        this.id = id;
        this.product = product;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.industry = industry;
        this.origin = origin;
    }

    // Getters e Setters (Data)
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

    // Método Builder (simples, para depuração)
    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private Long id;
        private String product;
        private String type;
        private BigDecimal price;
        private Integer quantity;
        private String industry;
        private String origin;

        ProductBuilder() {
        }

        public ProductBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProductBuilder product(String product) {
            this.product = product;
            return this;
        }

        public ProductBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ProductBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ProductBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public ProductBuilder industry(String industry) {
            this.industry = industry;
            return this;
        }

        public ProductBuilder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Product build() {
            return new Product(id, product, type, price, quantity, industry, origin);
        }

        public String toString() {
            return "Product.ProductBuilder(id=" + this.id + ", product=" + this.product + ", type=" + this.type + 
                   ", price=" + this.price + ", quantity=" + this.quantity + ", industry=" + this.industry + 
                   ", origin=" + this.origin + ")";
        }
    }
}