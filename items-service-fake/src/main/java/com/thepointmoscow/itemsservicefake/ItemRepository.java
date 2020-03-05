package com.thepointmoscow.itemsservicefake;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
    List<Item> findByTaxIdentity(String taxIdentity);
}
