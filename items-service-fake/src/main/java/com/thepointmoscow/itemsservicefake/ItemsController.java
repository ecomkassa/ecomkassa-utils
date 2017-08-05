package com.thepointmoscow.itemsservicefake;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/items", produces = "application/json")
@Slf4j
@Api("Items operations")
public class ItemsController {
    @Autowired
    private ItemRepository repo;

    @ApiOperation("Retrieves all the items")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Item> getAll() {
        List<Item> all = new LinkedList<>();
        repo.findAll().iterator().forEachRemaining(all::add);
        return all;
    }

    @ApiOperation("Retrieves the concrete single item")
    @RequestMapping(value = "/{itemId}", method = RequestMethod.GET)
    public Item getItem(@PathVariable("itemId") String itemId) {
        return repo.findOne(itemId);
    }

    @ApiOperation(value = "Tries to reserve an item",
            notes = "If item quantity more than 3 pieces, then will fail")
    @RequestMapping(value = "/{itemId}", method = RequestMethod.POST)
    public ResponseEntity<RestResponse> reserveItem(@PathVariable("itemId") String itemId,
                                                    @RequestParam(value = "qty", defaultValue = "1") Integer quantity) {
        Item item = repo.findOne(itemId);
        final boolean success = (item != null && quantity <= 3);
        return ResponseEntity.ok(new RestResponse(success));
    }

    @ApiOperation("Makes item search")
    @ApiImplicitParams({
            @ApiImplicitParam("name"), @ApiImplicitParam("sku"), @ApiImplicitParam("size")
    })
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public List<Item> searchItems(@RequestParam Map<String, String> params) {
        List<Item> all = getAll();
        if (params == null || params.isEmpty())
            return all;

        List<Predicate<? super Item>> predicates = new LinkedList<>();
        if (params.get("sku") != null) {
            predicates.add(it -> it.getSku().equalsIgnoreCase(params.get("sku")));
        }
        if (params.get("size") != null) {
            predicates.add(it -> it.getSize().equalsIgnoreCase(params.get("size")));
        }
        if (params.get("name") != null) {
            predicates.add(it -> it.getName().toLowerCase().contains(params.get("name").toLowerCase()));
        }
        Stream<Item> allStream = all.stream();
        for (Predicate<? super Item> predicate : predicates) {
            allStream = allStream.filter(predicate);
        }
        return allStream.collect(Collectors.toList());
    }
}
