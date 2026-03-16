package com.huyuans.bailian.router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;

public class SemanticRouter {

    private static final Logger log = Logger.getLogger(SemanticRouter.class.getName());

    private final List<Route> routes;
    private final Map<String, Double> intentScores;
    private final Route defaultRoute;

    public SemanticRouter(List<Route> routes, Route defaultRoute) {
        this.routes = new ArrayList<>(routes);
        this.defaultRoute = defaultRoute;
        this.intentScores = new ConcurrentHashMap<>();
        log.info("语义路由器初始化完成, 共" + routes.size() + "条路由");
    }

    public RoutingResult route(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new RoutingResult(defaultRoute.getName(), 0.0, false, null);
        }

        String normalizedInput = normalizeInput(input);
        double maxScore = -1;
        Route matchedRoute = null;

        for (Route route : routes) {
            double score = calculateScore(normalizedInput, route);
            if (score > maxScore) {
                maxScore = score;
                matchedRoute = route;
            }
        }

        if (matchedRoute != null && maxScore >= matchedRoute.getThreshold()) {
            log.info("匹配路由: '" + matchedRoute.getName() + "' (置信度: " + maxScore + ")");
            return new RoutingResult(matchedRoute.getName(), maxScore, true, matchedRoute.getHandler());
        }

        return new RoutingResult(defaultRoute.getName(), 0.0, false, null);
    }

    public Object execute(String input) {
        RoutingResult result = route(input);
        if (result.getHandler() != null) {
            return result.getHandler().apply(input);
        }
        return null;
    }

    private double calculateScore(String input, Route route) {
        double score = 0.0;
        String lowerInput = input.toLowerCase();

        for (String keyword : route.getKeywords()) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                score += route.getKeywordWeight();
            }
        }

        for (String pattern : route.getPatterns()) {
            if (matchesPattern(lowerInput, pattern.toLowerCase())) {
                score += route.getPatternWeight();
            }
        }

        for (String exact : route.getExactMatches()) {
            if (lowerInput.equals(exact.toLowerCase())) {
                score += route.getExactWeight();
            }
        }

        if (route.getNegativeKeywords() != null) {
            for (String negative : route.getNegativeKeywords()) {
                if (lowerInput.contains(negative.toLowerCase())) {
                    score -= route.getNegativeWeight();
                }
            }
        }

        int totalIndicators = route.getKeywords().size() + route.getPatterns().size() + route.getExactMatches().size();
        if (totalIndicators > 0) {
            score = Math.min(1.0, score / (totalIndicators * 0.5));
        }

        return Math.max(0.0, Math.min(1.0, score));
    }

    private boolean matchesPattern(String input, String pattern) {
        if (pattern.contains("*")) {
            String regex = pattern.replace("*", ".*");
            return input.matches(".*" + regex + ".*");
        }
        return input.contains(pattern);
    }

    private String normalizeInput(String input) {
        return input.trim().replaceAll("\\s+", " ");
    }

    public void addRoute(Route route) {
        routes.add(route);
        log.info("添加路由: " + route.getName());
    }

    public boolean removeRoute(String routeName) {
        boolean removed = routes.removeIf(r -> r.getName().equals(routeName));
        if (removed) {
            log.info("移除路由: " + routeName);
        }
        return removed;
    }

    public static class Route {
        private final String name;
        private final List<String> keywords;
        private final List<String> patterns;
        private final List<String> exactMatches;
        private final List<String> negativeKeywords;
        private final double threshold;
        private final double keywordWeight;
        private final double patternWeight;
        private final double exactWeight;
        private final double negativeWeight;
        private final Function<String, Object> handler;

        public Route(String name, List<String> keywords, List<String> patterns, List<String> exactMatches,
                    List<String> negativeKeywords, double threshold, double keywordWeight, double patternWeight,
                    double exactWeight, double negativeWeight, Function<String, Object> handler) {
            this.name = name;
            this.keywords = keywords;
            this.patterns = patterns;
            this.exactMatches = exactMatches;
            this.negativeKeywords = negativeKeywords;
            this.threshold = threshold;
            this.keywordWeight = keywordWeight;
            this.patternWeight = patternWeight;
            this.exactWeight = exactWeight;
            this.negativeWeight = negativeWeight;
            this.handler = handler;
        }

        public String getName() { return name; }
        public List<String> getKeywords() { return keywords; }
        public List<String> getPatterns() { return patterns; }
        public List<String> getExactMatches() { return exactMatches; }
        public List<String> getNegativeKeywords() { return negativeKeywords; }
        public double getThreshold() { return threshold; }
        public double getKeywordWeight() { return keywordWeight; }
        public double getPatternWeight() { return patternWeight; }
        public double getExactWeight() { return exactWeight; }
        public double getNegativeWeight() { return negativeWeight; }
        public Function<String, Object> getHandler() { return handler; }

        public static Route of(String name, List<String> keywords, Function<String, Object> handler) {
            return new Route(name, keywords, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                    0.5, 0.3, 0.5, 1.0, 0.5, handler);
        }

        public static Route of(String name, String... keywords) {
            return of(name, Arrays.asList(keywords), null);
        }
    }

    public static class RoutingResult {
        private final String routeName;
        private final double confidence;
        private final boolean matched;
        private final Function<String, Object> handler;

        public RoutingResult(String routeName, double confidence, boolean matched, Function<String, Object> handler) {
            this.routeName = routeName;
            this.confidence = confidence;
            this.matched = matched;
            this.handler = handler;
        }

        public String getRouteName() { return routeName; }
        public double getConfidence() { return confidence; }
        public boolean isMatched() { return matched; }
        public Function<String, Object> getHandler() { return handler; }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Route> routes = new ArrayList<>();
        private Route defaultRoute = Route.of("default", "default");

        public Builder route(Route route) {
            routes.add(route);
            return this;
        }

        public Builder defaultRoute(Route route) {
            this.defaultRoute = route;
            return this;
        }

        public SemanticRouter build() {
            return new SemanticRouter(routes, defaultRoute);
        }
    }
}