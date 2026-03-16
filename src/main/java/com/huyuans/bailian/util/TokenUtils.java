package com.huyuans.bailian.util;

import java.util.List;
import java.util.regex.Pattern;
































public class TokenUtils {

    
    private TokenUtils() {}

    


    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fff\\u3400-\\u4dbf]");

    










    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int chineseCount = 0;
        int wordCount = 0;
        int otherCount = 0;

        StringBuilder currentWord = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (CHINESE_PATTERN.matcher(String.valueOf(c)).matches()) {
                
                if (currentWord.length() > 0) {
                    wordCount++;
                    currentWord.setLength(0);
                }
                chineseCount++;
            } else if (Character.isLetterOrDigit(c)) {
                
                currentWord.append(c);
            } else {
                
                if (currentWord.length() > 0) {
                    wordCount++;
                    currentWord.setLength(0);
                }
                otherCount++;
            }
        }

        
        if (currentWord.length() > 0) {
            wordCount++;
        }

        
        
        
        
        return (int) Math.ceil(chineseCount * 1.5 + wordCount * 1.3 + otherCount * 0.5);
    }

    





    public static int estimateMessagesTokens(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }

        int total = 0;
        for (String message : messages) {
            total += estimateTokens(message);
            
            total += 4;
        }

        
        return total + 3;
    }

    








    public static int suggestOutputTokens(int inputTokens, double ratio) {
        return (int) Math.ceil(inputTokens * ratio);
    }

    







    public static int suggestMaxTokens(int inputTokens) {
        return suggestOutputTokens(inputTokens, 1.0);
    }

    






    public static boolean exceedsLimit(String text, int limit) {
        return estimateTokens(text) > limit;
    }

    






    public static String truncateToLimit(String text, int limit) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        if (!exceedsLimit(text, limit)) {
            return text;
        }

        
        double avgTokensPerChar = (double) estimateTokens(text) / text.length();
        
        int targetChars = (int) ((limit * 0.9) / avgTokensPerChar);

        if (targetChars >= text.length()) {
            return text;
        }

        
        int lastPeriod = text.lastIndexOf('。');
        int lastQuestion = text.lastIndexOf('？');
        int lastExclaim = text.lastIndexOf('！');
        int lastDot = text.lastIndexOf('.');

        int lastSentenceEnd = Math.max(Math.max(lastPeriod, lastQuestion), 
                                        Math.max(lastExclaim, lastDot));

        if (lastSentenceEnd > targetChars * 0.7 && lastSentenceEnd < targetChars * 1.3) {
            return text.substring(0, lastSentenceEnd + 1);
        }

        return text.substring(0, targetChars);
    }

    





    public static String formatTokens(int tokens) {
        if (tokens < 1000) {
            return tokens + " tokens";
        } else if (tokens < 1000000) {
            return String.format("%.1fK tokens", tokens / 1000.0);
        } else {
            return String.format("%.2fM tokens", tokens / 1000000.0);
        }
    }
}