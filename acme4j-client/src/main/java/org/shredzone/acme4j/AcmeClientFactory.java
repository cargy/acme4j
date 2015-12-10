/*
 * acme4j - Java ACME client
 *
 * Copyright (C) 2015 Richard "Shred" Körber
 *   http://acme4j.shredzone.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.shredzone.acme4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.provider.AcmeClientProvider;

/**
 * Generates {@link AcmeClient} instances.
 * <p>
 * An {@link AcmeClient} is generated by an {@link AcmeClientProvider}. There are generic
 * providers and providers tailor-made for specific ACME servers. All providers are
 * managed via Java's {@link ServiceLoader} API.
 *
 * @author Richard "Shred" Körber
 */
public final class AcmeClientFactory {

    private AcmeClientFactory() {
        // utility class without constructor
    }

    /**
     * Connects to an ACME server and provides an {@link AcmeClient} for communication.
     *
     * @param serverUri
     *            URI of the ACME server. This can either be a http/https URI to the
     *            server's directory service, or a special acme URI for specific
     *            implementations.
     * @return {@link AcmeClient} for communication with the server
     */
    public static AcmeClient connect(String serverUri) throws AcmeException {
        try {
            return connect(new URI(serverUri));
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Connects to an ACME server and provides an {@link AcmeClient} for communication.
     *
     * @param serverUri
     *            URI of the ACME server. This can either be a http/https URI to the
     *            server's directory service, or a special acme URI for specific
     *            implementations.
     * @return {@link AcmeClient} for communication with the server
     */
    public static AcmeClient connect(URI serverUri) throws AcmeException {
        List<AcmeClientProvider> candidates = new ArrayList<>();
        for (AcmeClientProvider acp : ServiceLoader.load(AcmeClientProvider.class)) {
            if (acp.accepts(serverUri)) {
                candidates.add(acp);
            }
        }

        if (candidates.isEmpty()) {
            throw new AcmeException("No ACME provider found for " + serverUri);
        } else if (candidates.size() > 1) {
            throw new IllegalArgumentException("There are " + candidates.size() + " "
                + AcmeClientProvider.class.getSimpleName() + " accepting " + serverUri
                + ". Please check your classpath.");
        } else {
            AcmeClientProvider provider = candidates.get(0);
            return provider.connect(serverUri);
        }
    }

}
