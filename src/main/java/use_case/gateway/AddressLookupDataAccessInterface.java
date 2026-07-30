package use_case.gateway;

import entity.GeoPoint;

/** Resolves a human-readable address to a map coordinate. */
public interface AddressLookupDataAccessInterface {
    GeoPoint lookup(String address);
}
