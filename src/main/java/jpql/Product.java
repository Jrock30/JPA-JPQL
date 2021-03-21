package jpql;

import javax.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int price;
    private int stockAmount;

}
