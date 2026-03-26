package com.tramplin.backend.model;

public enum OpportunityStatus {
    OPEN,      // Активна, видна всем, можно откликаться
    ARCHIVED,  // Срок вышел или набор закрыт, видна в истории, откликаться нельзя
    DELETED    // Удалена работодателем, скрыта из поиска, но остается в истории откликов
}