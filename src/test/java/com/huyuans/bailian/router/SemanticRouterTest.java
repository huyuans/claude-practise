package com.huyuans.bailian.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SemanticRouter.
 */
class SemanticRouterTest {

    private SemanticRouter router;

    @BeforeEach
    void setUp() {
        Function<String, Object> chatHandler = input -> "chat:" + input;

        SemanticRouter.Route chatRoute = new SemanticRouter.Route(
                "chat", Arrays.asList("你好", "hello"), Arrays.asList(), Arrays.asList(), null,
                0.2, 0.5, 0.5, 1.0, 0.5, chatHandler);

        router = SemanticRouter.builder()
                .route(chatRoute)
                .defaultRoute(SemanticRouter.Route.of("default", "default"))
                .build();
    }

    @Test
    void testRoute() {
        SemanticRouter.RoutingResult result = router.route("你好");
        assertEquals("chat", result.getRouteName());
    }

    @Test
    void testDefaultRoute() {
        SemanticRouter.RoutingResult result = router.route("xyz");
        assertEquals("default", result.getRouteName());
    }

    @Test
    void testEmptyInput() {
        SemanticRouter.RoutingResult result = router.route("");
        assertEquals("default", result.getRouteName());
    }
}
