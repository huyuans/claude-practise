package com.huyuans.bailian.router;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("语义路由器测试")
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
    @DisplayName("路由测试")
    void testRoute() {
        SemanticRouter.RoutingResult result = router.route("你好");
        assertEquals("chat", result.getRouteName());
    }

    @Test
    @DisplayName("默认路由测试")
    void testDefaultRoute() {
        SemanticRouter.RoutingResult result = router.route("xyz");
        assertEquals("default", result.getRouteName());
    }

    @Test
    @DisplayName("空输入测试")
    void testEmptyInput() {
        SemanticRouter.RoutingResult result = router.route("");
        assertEquals("default", result.getRouteName());
    }
}