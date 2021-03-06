package org.openstack4j.openstack.internal;

import static org.openstack4j.core.transport.HttpExceptionHandler.mapException;

import org.openstack4j.api.OSClient;
import org.openstack4j.api.OSClient.OSClientV2;
import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.api.client.CloudProvider;
import org.openstack4j.api.types.Facing;
import org.openstack4j.core.transport.*;
import org.openstack4j.core.transport.internal.HttpExecutor;
import org.openstack4j.model.identity.AuthStore;
import org.openstack4j.model.identity.AuthVersion;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.common.Auth;
import org.openstack4j.openstack.common.Auth.Type;
import org.openstack4j.openstack.identity.v2.domain.Credentials;
import org.openstack4j.openstack.identity.v2.domain.KeystoneAccess;
import org.openstack4j.openstack.identity.v2.domain.RaxApiKeyCredentials;
import org.openstack4j.openstack.identity.v3.domain.KeystoneAuth;
import org.openstack4j.openstack.identity.v3.domain.KeystoneToken;
import org.openstack4j.openstack.identity.v3.domain.TokenAuth;
import org.openstack4j.openstack.internal.OSClientSession.OSClientSessionV2;
import org.openstack4j.openstack.internal.OSClientSession.OSClientSessionV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for authenticating and re-authenticating sessions for V2 and V3
 * of the Identity API
 */
public class OSAuthenticator {

    private static final String TOKEN_INDICATOR = "Tokens";
    private static final Logger LOG = LoggerFactory.getLogger(OSAuthenticator.class);

    /**
     * Invokes authentication to obtain a valid V3 Token, throws an
     * UnsupportedOperationException for an V2 attempt.
     *
     * @param auth the authentication credentials
     * @param endpoint the identity endpoint
     * @param perspective the network facing perspective
     * @param config the client configuration
     * @return the OSClient
     */
    @SuppressWarnings("rawtypes")
    public static OSClient invoke(AuthStore auth, String endpoint, Facing perspective, Config config,
            CloudProvider provider) {
        SessionInfo info = new SessionInfo(endpoint, perspective, false, provider);
        if (auth.getVersion() == AuthVersion.V2) {
            return authenticateV2((org.openstack4j.openstack.identity.v2.domain.Auth) auth, info, config);
        }
        return authenticateV3((KeystoneAuth) auth, info, config);
    }

    /**
     * Invokes V3 authentication via an existing token
     * 
     * @param auth the token authentication
     * @param endpoint the identity endpoint
     * @param perspective the network facing perspective
     * @param config the client configuration
     * @return the OSClient
     */
    @SuppressWarnings("rawtypes")
    public static OSClient invoke(KeystoneAuth auth, String endpoint, Facing perspective, Config config,
            CloudProvider provider) {
        SessionInfo info = new SessionInfo(endpoint, perspective, false, provider);
        return authenticateV3(auth, info, config);
    }

    /**
     * Invokes V2 authentication via an existing token
     * 
     * @param auth the token authentication
     * @param endpoint the identity endpoint
     * @param perspective the network facing perspective
     * @param config the client configuration
     * @return the OSClient
     */
    @SuppressWarnings("rawtypes")
    public static OSClient invoke(org.openstack4j.openstack.identity.v2.domain.TokenAuth auth, String endpoint,
            Facing perspective, Config config, CloudProvider provider) {
        SessionInfo info = new SessionInfo(endpoint, perspective, false, provider);
        return authenticateV2(auth, info, config);
    }

    /**
     * Re-authenticates/renews the token for the current Session
     */
    @SuppressWarnings("rawtypes")
    public static void reAuthenticate() {

        LOG.debug("Re-Authenticating session due to expired Token or invalid response");

        OSClientSession session = OSClientSession.getCurrent();

        switch (session.getAuthVersion()) {
        case V2:
            KeystoneAccess access = ((OSClientSessionV2) session).getAccess().unwrap();
            SessionInfo info = new SessionInfo(access.getEndpoint(), session.getPerspective(), true,
                    session.getProvider());
            Auth auth = (Auth) ((access.isCredentialType()) ? access.getCredentials() : access.getTokenAuth());
            authenticateV2((org.openstack4j.openstack.identity.v2.domain.Auth) auth, info, session.getConfig());
            break;
        case V3:
        default:
            Token token = ((OSClientSessionV3) session).getToken();
            info = new SessionInfo(token.getEndpoint(), session.getPerspective(), true, session.getProvider());
            authenticateV3((KeystoneAuth) token.getCredentials(), info, session.getConfig());
            break;
        }
    }

    private static OSClientV2 authenticateV2(org.openstack4j.openstack.identity.v2.domain.Auth auth, SessionInfo info,
            Config config) {
        HttpRequest<KeystoneAccess> request = HttpRequest.builder(KeystoneAccess.class)
                .header(ClientConstants.HEADER_OS4J_AUTH, TOKEN_INDICATOR).endpoint(info.endpoint)
                .method(HttpMethod.POST).path("/tokens").config(config).entity(auth).build();

        HttpResponse response = HttpExecutor.create().execute(request);
        if (response.getStatus() >= 400) {
            try {
                throw mapException(response.getStatusMessage(), response.getStatus());
            } finally {
                HttpEntityHandler.closeQuietly(response);
            }
        }

        KeystoneAccess access = response.getEntity(KeystoneAccess.class);

        if (auth.getType() == Type.CREDENTIALS) {
            access = access.applyContext(info.endpoint, (Credentials) auth);
        } else if (auth.getType() == Type.RAX_APIKEY) {
            access = access.applyContext(info.endpoint, (RaxApiKeyCredentials) auth);
        } else {
            access = access.applyContext(info.endpoint, (org.openstack4j.openstack.identity.v2.domain.TokenAuth) auth);
        }

        if (!info.reLinkToExistingSession)
            return OSClientSession.OSClientSessionV2.createSession(access, info.perspective, info.provider, config);

        OSClientSession.OSClientSessionV2 current = (OSClientSessionV2) OSClientSession.getCurrent();
        current.access = access;
        return current;
    }

    private static OSClientV3 authenticateV3(KeystoneAuth auth, SessionInfo info, Config config) {
        HttpRequest<KeystoneToken> request = HttpRequest.builder(KeystoneToken.class)
                .header(ClientConstants.HEADER_OS4J_AUTH, TOKEN_INDICATOR).endpoint(info.endpoint)
                .method(HttpMethod.POST).path("/auth/tokens").config(config).entity(auth).build();

        HttpResponse response = HttpExecutor.create().execute(request);

        if (response.getStatus() >= 400) {
            try {
                throw mapException(response.getStatusMessage(), response.getStatus());
            } finally {
                HttpEntityHandler.closeQuietly(response);
            }
        }
        KeystoneToken token = response.getEntity(KeystoneToken.class);
        token.setId(response.header(ClientConstants.HEADER_X_SUBJECT_TOKEN));

        if (auth.getType() == Type.CREDENTIALS) {
            token = token.applyContext(info.endpoint, auth);
        } else {
            if (token.getProject() != null) {
                token = token.applyContext(info.endpoint, new TokenAuth(token.getId(),
                        auth.getScope().getProject().getName(), auth.getScope().getProject().getId()));
            } else {
                token = token.applyContext(info.endpoint, new TokenAuth(token.getId(),
                        auth.getScope().getDomain().getName(), auth.getScope().getDomain().getId()));
            }
        }

        if (!info.reLinkToExistingSession)
            return OSClientSessionV3.createSession(token, info.perspective, info.provider, config);

        OSClientSessionV3 current = (OSClientSessionV3) OSClientSessionV3.getCurrent();
        current.token = token;
        return current;
    }

    private static class SessionInfo {
        String endpoint;
        Facing perspective;
        boolean reLinkToExistingSession;
        CloudProvider provider;

        SessionInfo(String endpoint, Facing perspective, boolean reLinkToExistingSession, CloudProvider provider) {
            this.endpoint = endpoint;
            this.perspective = perspective;
            this.reLinkToExistingSession = reLinkToExistingSession;
            this.provider = provider;
        }
    }
}
