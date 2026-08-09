package use_case.port;

import entity.GeoPoint;

/**
 * Resolves a human-readable address to a map coordinate.
 */
public interface AddressLookupGateway {
    GeoPoint lookup(String address);
}
