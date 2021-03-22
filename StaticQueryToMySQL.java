package com.coffeeshop.repository;

import com.coffeeshop.model.web.productslist.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

@Repository
@PropertySource(value = "classpath:defaultFindProduct.properties")
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    @PersistenceContext
    EntityManager entityManager;

    @Value(value = "${default.price.min}")
    private Double PRICE_MIN;

    @Value(value = "${default.price.max}")
    private Double PRICE_MAX;

    @Value(value = "${default.bitter.from}")
    private Integer BITTER_FROM;

    @Value(value = "${default.bitter.to}")
    private Integer BITTER_TO;

    @Value(value = "${default.sour.from}")
    private Integer SOUR_FROM;

    @Value(value = "${default.sour.to}")
    private Integer SOUR_TO;

    @Value(value = "${default.strong.from}")
    private Integer STRONG_FROM;
    
    @Value(value = "${default.strong.to}")
    private Integer STRONG_TO;

    @Override
    public ProductListDtoResponse searchProductViaParameter(ProductListDtoRequest productListDtoRequest) {

        TypedQuery<Object[]> typedQuery = entityManager.createQuery(Query(), Object[].class);

        String arr[] = productListDtoRequest.getSearch().split(" ");
        productListDtoRequest.setSearch(arr[0]);

        typedQuery.setParameter("search", productListDtoRequest.getSearch() == null
                ? "" : productListDtoRequest.getSearch());

        typedQuery.setParameter("priceMin", productListDtoRequest.getPriceMin()
                <= 0 ? PRICE_MIN : productListDtoRequest.getPriceMin());
        
        typedQuery.setParameter("priceMax", productListDtoRequest.getPriceMax()
                > PRICE_MAX ? PRICE_MAX : productListDtoRequest.getPriceMax());
        
        typedQuery.setParameter("bitterFrom", productListDtoRequest.getCharacteristics().getBitterFrom()
                < BITTER_FROM ? BITTER_FROM : productListDtoRequest.getCharacteristics().getBitterFrom());
        
        typedQuery.setParameter("bitterTo", productListDtoRequest.getCharacteristics().getBitterTo()
                > BITTER_TO ? BITTER_TO : productListDtoRequest.getCharacteristics().getBitterTo());
        
        typedQuery.setParameter("sourFrom", productListDtoRequest.getCharacteristics().getSourFrom()
                < SOUR_FROM ? SOUR_FROM : productListDtoRequest.getCharacteristics().getSourFrom());
        
        typedQuery.setParameter("sourTo", productListDtoRequest.getCharacteristics().getSourTo()
                > SOUR_TO ? SOUR_TO : productListDtoRequest.getCharacteristics().getSourTo());
        
        typedQuery.setParameter("strongFrom", productListDtoRequest.getCharacteristics().getStrongFrom()
                < STRONG_FROM ? SOUR_FROM : productListDtoRequest.getCharacteristics().getStrongFrom());
        
        typedQuery.setParameter("strongTo", productListDtoRequest.getCharacteristics().getStrongTo()
                > STRONG_TO ? STRONG_TO : productListDtoRequest.getCharacteristics().getStrongTo());
        
        typedQuery.setParameter("decaf", productListDtoRequest.getCharacteristics().isDecaf());
        typedQuery.setParameter("ground", productListDtoRequest.getCharacteristics().isGround());
        
        // After creating a method for determining popularity, will need to add the parameter "popular" into "sortBy".
        typedQuery.setParameter("sortBy", productListDtoRequest.getSortBy().equals(null) ? "pc.product.id" :
                productListDtoRequest.getSortBy().equals("price") ? "p.unitPrice" : "p.productName");
        
        typedQuery.setFirstResult((productListDtoRequest.getPage() - 1) * productListDtoRequest.getResults());
        typedQuery.setMaxResults(productListDtoRequest.getResults());

        List<Object[]> list = null;
        try {
            list = typedQuery.getResultList();
        } catch (NoResultException e) {
            e.printStackTrace();
        }

        ProductListDtoResponse productListDtoResponse = new ProductListDtoResponse();

        for (Object[] num : list) {
            ProductDescriptionDto productDescriptionDto = null;
            try {
                productDescriptionDto = ProductDescriptionDto.builder()
                        .productId((Long) num[0])
                        .title(num[1].toString())
                        .shortDescription(num[2].toString())
                        .type(num[3].toString())
                        .previewImage(num[4].toString())
                        .price((Double) num[5])
                        .availableAmount((Integer) num[6])
                        .productParameters(
                                ProductParametersDto.builder()
                                        .bitter((Integer) num[7])
                                        .sour((Integer) num[8])
                                        .strong((Integer) num[9])
                                        .decaf((Boolean) num[10])
                                        .ground((Boolean) num[11])
                                        .build()
                        ).build();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }

            List<ProductDescriptionDto> listProduct = new ArrayList<>();
            listProduct.add(productDescriptionDto);

            productListDtoResponse = ProductListDtoResponse.builder()
                    .popular(productDescriptionDto)
                    .products(listProduct)
                    .build();
        }
        return productListDtoResponse;
    }

    private String Query() {
        return new StringBuilder()
                .append("SELECT ")
                .append("pc.product.id, p.productName, p.shortDescription, p.productCategory, ")
                .append("p.previewImage, p.unitPrice, pq.quantity, pc.strong, pc.sour, pc.bitter, pc.decaf, pc.ground ")
                .append("FROM com.coffeeshop.model.entity.product.Product p ")
                .append("JOIN com.coffeeshop.model.entity.product.ProductCoffee pc ON p.id = pc.product.id ")
                .append("JOIN com.coffeeshop.model.entity.product.ProductQuantity pq ON pc.product.id = pq.product.id ")
                .append("WHERE p.productName LIKE :search ")
                .append("AND p.available = true ")
                .append("AND p.unitPrice >= :priceMin ")
                .append("AND p.unitPrice <= :priceMax ")
                .append("AND pc.bitter BETWEEN :bitterFrom AND :bitterTo ")
                .append("AND pc.sour BETWEEN :sourFrom AND :sourTo ")
                .append("AND pc.strong BETWEEN :strongFrom AND :strongTo ")
                .append("AND pc.decaf = :decaf ")
                .append("AND pc.ground = :ground ")
                .append("ORDER BY :sortBy ")
                .toString();
    }
}
