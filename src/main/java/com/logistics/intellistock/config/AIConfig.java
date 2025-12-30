package com.logistics.intellistock.config;

import com.logistics.intellistock.ai.analysis.SeasonalityDetector;
import com.logistics.intellistock.ai.analysis.TrendAnalyzer;
import com.logistics.intellistock.ai.engine.PredictionEngine;
import com.logistics.intellistock.ai.llm.AiRecommendationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AIConfig {

    @Bean
    public SeasonalityDetector seasonalityDetector() {
        return new SeasonalityDetector();
    }

    @Bean
    public TrendAnalyzer trendAnalyzer() {
        return new TrendAnalyzer();
    }

    @Bean
    public AiRecommendationService aiRecommendationService() {
        return new AiRecommendationService();
    }
}