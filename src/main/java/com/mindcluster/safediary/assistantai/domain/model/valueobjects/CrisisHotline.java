package com.mindcluster.safediary.assistantai.domain.model.valueobjects;

/**
 * Emergency hotline offered to the user when the crisis protocol is activated.
 *
 * @param name        hotline display name
 * @param phone       number to dial
 * @param description short description of the service
 */
public record CrisisHotline(String name, String phone, String description) {
}
