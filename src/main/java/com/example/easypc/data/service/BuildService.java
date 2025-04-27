package com.example.easypc.data.service;

import com.example.easypc.data.dto.BuildDto;
import com.example.easypc.data.dto.BuildItemDto;
import com.example.easypc.data.entity.Build;
import com.example.easypc.data.entity.BuildItem;
import com.example.easypc.data.entity.Cart;
import com.example.easypc.data.entity.User;
import com.example.easypc.data.repository.BuildItemRepository;
import com.example.easypc.data.repository.BuildRepository;
import com.example.easypc.data.repository.CartRepository;
import com.example.easypc.parse.ProductData;
import com.example.easypc.parse.ProductParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class BuildService {

    @Autowired
    private BuildRepository buildRepository;

    @Autowired
    private BuildItemRepository buildItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductParserService productParserService;

    @Autowired
    private CartService cartService;

    @Transactional
    public void saveCartToBuild(Long userId, String buildName) {
        Build build = buildRepository.findByUserIdAndName(userId, buildName);
        if (build == null) {
            build = new Build();
            build.setName(buildName);
            build.setUserId(userId);
            build.setTotalPrice(Double.valueOf(String.valueOf(cartService.getCartTotal(userId))));
            buildRepository.save(build);
        }

        List<Cart> cartItems = cartRepository.findByUserId(userId);

        for (Cart cartItem : cartItems) {
            BuildItem buildItem = new BuildItem();
            buildItem.setBuild(build);
            buildItem.setProductUrl(cartItem.getSource());
            buildItem.setQuantity(cartItem.getQuantity());

            buildItemRepository.save(buildItem);
        }
    }

    public List<BuildDto> getUserBuilds(Long userId) {
        List<Build> builds = buildRepository.findByUserId(userId);

        return builds.stream().map(build -> {
            List<BuildItemDto> items = build.getItems().stream().map(item -> {
                Long sourceId = Long.valueOf(item.getProductUrl().getId()); // Получаем id ссылки
                ProductData productData = productParserService.parseSingleProduct(sourceId);
                return new BuildItemDto(
                        item.getId(),
                        productData.getName(),
                        productData.getPrice(),
                        productData.getImg(),
                        item.getQuantity()
                );
            }).toList();

            return new BuildDto(build.getId(), build.getName(), build.getTotalPrice(), items);
        }).toList();
    }

    public Optional<Build> findByIdAndUser(Long buildId, Long userId) {
        return buildRepository.findByIdAndUserId(buildId, userId);
    }

    @Transactional
    public void deleteBuild(Build build) {
        buildItemRepository.deleteByBuild(build);
        buildRepository.delete(build);
    }

    public void addBuildItemsToCart(User user, List<BuildItem> buildItems) {
        for (BuildItem buildItem : buildItems) {
            cartRepository.findByUserIdAndSourceId(user.getId(), Long.valueOf(buildItem.getProductUrl().getId()))
                    .ifPresentOrElse(
                            existingCartItem -> {
                                existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
                                cartRepository.save(existingCartItem);
                            },
                            () -> {
                                Cart newCartItem = new Cart();
                                newCartItem.setUser(user);
                                newCartItem.setSource(buildItem.getProductUrl());
                                newCartItem.setQuantity(1);
                                cartRepository.save(newCartItem);
                            }
                    );
        }
    }
}