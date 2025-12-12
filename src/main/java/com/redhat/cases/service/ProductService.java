package com.redhat.cases.service;

import com.redhat.cases.config.RedHatApiConfig;
import com.redhat.cases.client.HydraClient;
import com.redhat.cases.client.RedHatAuthClient;
import com.redhat.cases.dto.ProductDto;
import com.redhat.cases.dto.VersionDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Application service for Red Hat product information.
 * Provides product and version listings for case creation.
 */
@ApplicationScoped
public class ProductService {

    private final RedHatApiConfig config;
    private final HydraClient hydraClient;
    private final RedHatAuthClient authClient;

    @Inject
    public ProductService(RedHatApiConfig config, HydraClient hydraClient, RedHatAuthClient authClient) {
        this.config = config;
        this.hydraClient = hydraClient;
        this.authClient = authClient;
    }

    /**
     * Verifies if the service is correctly configured.
     */
    public boolean isConfigured() {
        return authClient.isConfigured();
    }

    /**
     * Gets all available Red Hat products for case creation.
     * Only returns active products, sorted by product line and name.
     *
     * @return List of available products
     */
    public List<ProductDto> getProducts() {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        List<ProductDto> products = hydraClient.getProducts();

        return products.stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .sorted(Comparator
                .comparing((ProductDto p) -> p.getLine() != null ? p.getLine() : "")
                .thenComparing(ProductDto::getName))
            .toList();
    }

    /**
     * Gets available versions for a specific product.
     *
     * @param productCode The product code (from getProducts)
     * @return List of available versions
     */
    public List<VersionDto> getVersions(String productCode) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        if (productCode == null || productCode.isBlank()) {
            return Collections.emptyList();
        }

        return hydraClient.getProductVersions(productCode);
    }

    /**
     * Gets the default version for a product.
     *
     * @param productCode The product code
     * @return The default version name, or null if not found
     */
    public String getDefaultVersion(String productCode) {
        return getVersions(productCode).stream()
            .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
            .map(VersionDto::getName)
            .findFirst()
            .orElse(null);
    }

    /**
     * Validates if a product exists.
     *
     * @param productName The product name to validate
     * @return true if the product exists
     */
    public boolean isValidProduct(String productName) {
        return getProducts().stream()
            .anyMatch(p -> p.getName().equalsIgnoreCase(productName) ||
                          p.getCode().equalsIgnoreCase(productName));
    }
}
