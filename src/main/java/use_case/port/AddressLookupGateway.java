package use_case.port;

import entity.GeoPoint;

/**
 * Resolves a human-readable address to a map coordinate.
 */
public interface AddressLookupGateway {
    /**
     * Performs this operation.
     *
     * @param address parameter value.
     *
     * @return the operation result.
     */
    GeoPoint lookup(String address);
}
