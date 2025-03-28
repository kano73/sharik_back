package com.mary.sharik.repository;

import com.mary.sharik.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    @Query("{ $and: [ "
            + " { $or: [ "
            + "   { 'name': { $regex: ?0, $options: 'i' } }, "
            + "   { 'description': { $regex: ?0, $options: 'i' } } "
            + " ] }, "
            + " { $expr: { $or: [ { $eq: [ ?1, null ] }, { $gte: [ '$price', ?1 ] } ] } }, "
            + " { $expr: { $or: [ { $eq: [ ?2, null ] }, { $lte: [ '$price', ?2 ] } ] } }, "
            + " { $or: [ { $expr: { $eq: [ ?#{#categories == null || #categories.empty}, true ] } }, { 'categories': { $in: ?3 } } ] } "
            + " ] }")
    Page<Product> searchProductsByFilter(String nameOrDescription,
                                         Integer priceFrom, Integer priceTo,
                                         List<String> categories, Pageable pageable);
}
