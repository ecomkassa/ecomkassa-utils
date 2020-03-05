package com.thepointmoscow.itemsservicefake;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/items", produces = "application/json")
@Api("Items operations")
@Slf4j
public class ItemsController {
    @Autowired
    private ItemRepository repo;

    @ApiOperation("Retrieves all the items")
    @GetMapping(value = "/inn/{taxIdentity}")
    public List<Item> getAll(@PathVariable String taxIdentity) {
        return repo.findByTaxIdentity(taxIdentity);
    }

    @ApiOperation("Retrieves the concrete single item")
    @GetMapping(value = "/{itemId}")
    public Item getItem(@PathVariable("itemId") Long itemId) {
        return repo.findOne(itemId);
    }

    @ApiOperation(value = "Tries to reserve an item",
            notes = "If item quantity more than 3 pieces, then will fail")
    @PostMapping(value = "/{itemId}")
    public ResponseEntity<RestResponse> reserveItem(
            @PathVariable("itemId") Long itemId
            , @RequestParam(value = "qty", defaultValue = "1") Integer quantity
    ) {
        Item item = repo.findOne(itemId);
        final boolean success = (item != null && quantity <= 3);
        return ResponseEntity.ok(new RestResponse(success));
    }

    @ApiOperation("Retrieves an item image")
    @RequestMapping(value = "/{itemId}/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getItemImage(@PathVariable("itemId") Long itemId)
            throws IOException {

        InputStream img = getClass().getResourceAsStream("/static/images/image_medium.jpg");
        MediaType mediaType = MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(mediaType).body(IOUtils.toByteArray(img));
    }

    @ApiOperation("Makes item search")
    @GetMapping(value = "/inn/{taxIdentity}/search")
    public List<Item> searchItems(
            @PathVariable String taxIdentity
            , @ApiParam @RequestParam(required = false) String name
            , @ApiParam @RequestParam(required = false) String sku
    ) {
        List<Item> all = getAll(taxIdentity);

        List<Predicate<? super Item>> predicates = new LinkedList<>();
        Optional.ofNullable(sku)
                .ifPresent(someSku -> predicates.add(it -> it.getSku().equalsIgnoreCase(someSku)));

        Optional.ofNullable(name)
                .map(String::toLowerCase)
                .ifPresent(nameLowered -> predicates.add(it -> it.getName().toLowerCase().contains(nameLowered)));

        Stream<Item> allStream = all.stream();
        for (Predicate<? super Item> predicate : predicates) {
            allStream = allStream.filter(predicate);
        }
        return allStream.collect(Collectors.toList());
    }
}
