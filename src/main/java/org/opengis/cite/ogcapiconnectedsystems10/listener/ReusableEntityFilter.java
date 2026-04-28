package org.opengis.cite.ogcapiconnectedsystems10.listener;

import java.io.IOException;

import org.glassfish.jersey.client.ClientResponse;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

/**
 * Buffers the (response) entity so it can be read multiple times.
 *
 * <p>
 * <strong>WARNING:</strong> The entity InputStream must be reset after each read attempt.
 * </p>
 *
 * <p>
 * Ported from ets-ogcapi-features10@java17Tomcat10TeamEngine6 (Jakarta EE 9
 * ClientResponseFilter SPI replaces Jersey-1 ClientFilter).
 * </p>
 */
public class ReusableEntityFilter implements ClientResponseFilter {

	/** {@inheritDoc} */
	@Override
	public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
		if (responseContext instanceof ClientResponse) {
			((ClientResponse) responseContext).bufferEntity();
		}
	}

}
