/**
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 * <p>
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.financial.services.accelerator.consent.mgt.endpoint.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.financial.services.accelerator.common.util.JWTUtils;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentConstants;
import org.wso2.financial.services.accelerator.consent.mgt.endpoint.utils.ConsentUtils;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.ConsentAdminHandler;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.builder.ConsentAdminBuilder;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.admin.model.ConsentAdminData;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentException;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ConsentExtensionExporter;
import org.wso2.financial.services.accelerator.consent.mgt.extensions.common.ResponseStatus;

import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * ConsentSearchEndpoint.
 * <p>
 * This specifies a REST API for consent search to be used at consent user and
 * customer service portals.
 */
@SuppressFBWarnings("JAXRS_ENDPOINT")
// Suppressed content - Endpoints
// Suppression reason - False Positive : These endpoints are secured with access
// control
// as defined in the IS deployment.toml file
// Suppressed warning count - 5
@Path("/admin")
public class ConsentAdminEndpoint {

    private static final Log log = LogFactory.getLog(ConsentAdminEndpoint.class);
    private static ConsentAdminHandler consentAdminHandler = null;

    public ConsentAdminEndpoint() {

        if (consentAdminHandler == null) {
            initializeConsentAdminHandler();
        }
    }

    private static void initializeConsentAdminHandler() {
        ConsentAdminBuilder consentAdminBuilder = ConsentExtensionExporter.getConsentAdminBuilder();

        if (consentAdminBuilder != null) {
            consentAdminHandler = consentAdminBuilder.getConsentAdminHandler();
        }

        if (consentAdminHandler != null) {
            log.info(String.format("Consent admin handler %s initialized",
                    consentAdminHandler.getClass().getName().replaceAll("\n\r", "")));
        } else {
            log.warn("Consent admin handler is null");
        }
    }

    /**
     * Search consent data.
     */
    @GET
    @Path("/search")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json; charset=utf-8" })
    public Response search(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        validateUserPermission(consentAdminData, "userIds");
        consentAdminHandler.handleSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Search consent status audit records.
     */
    @GET
    @Path("/search/consent-status-audit")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json; charset=utf-8" })
    public Response searchConsentStatusAudit(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentStatusAuditSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Search consent file.
     */
    @GET
    @Path("/search/consent-file")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json; charset=utf-8" })
    public Response searchConsentFile(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentFileSearch(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Get consent amendment history.
     */
    @GET
    @Path("/consent-amendment-history")
    @Consumes({ "application/json; charset=utf-8" })
    @Produces({ "application/json; charset=utf-8" })
    public Response getConsentAmendmentHistoryById(@Context HttpServletRequest request,
            @Context HttpServletResponse response, @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentUtils.getHeaders(request),
                uriInfo.getQueryParameters(), uriInfo.getAbsolutePath().getPath(), request, response);
        consentAdminHandler.handleConsentAmendmentHistoryRetrieval(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Revoke consent data.
     */
    @DELETE
    @Path("/revoke")
    @Consumes({ "application/x-www-form-urlencoded" })
    @Produces({ "application/json; charset=utf-8" })
    public Response revoke(@Context HttpServletRequest request, @Context HttpServletResponse response,
            @Context UriInfo uriInfo) {

        ConsentAdminData consentAdminData = new ConsentAdminData(ConsentUtils.getHeaders(request),
                ConsentUtils.getJSONObjectPayload(request), uriInfo.getQueryParameters(),
                uriInfo.getAbsolutePath().getPath(), request, response);
        validateUserPermission(consentAdminData, "userId");
        consentAdminHandler.handleRevoke(consentAdminData);
        return sendResponse(consentAdminData);
    }

    /**
     * Method to send response using the payload and response status.
     * 
     * @param consentAdminData Consent admin data
     * @return Response
     */
    private Response sendResponse(ConsentAdminData consentAdminData) {
        if (consentAdminData.getResponseStatus() != null) {
            if (consentAdminData.getResponsePayload() != null) {
                return Response.status(consentAdminData.getResponseStatus().getStatusCode())
                        .entity(consentAdminData.getResponsePayload().toString()).build();
            }
            return Response.status(consentAdminData.getResponseStatus().getStatusCode()).build();
        } else {
            log.debug("Response status or payload unavailable. Throwing exception");
            throw new ConsentException(ResponseStatus.INTERNAL_SERVER_ERROR, "Response data unavailable");
        }
    }

    /**
     * Method to validate the user permission. Validates access token scopes against users.
     *
     * @param consentAdminData consent admin data
     */
    private void validateUserPermission(ConsentAdminData consentAdminData, String userIdParamName) {
        try {
            String authToken = consentAdminData.getHeaders().get(ConsentConstants.AUTHORIZATION);
            String tokenBody = JWTUtils.decodeRequestJWT(authToken.replace("Bearer ", ""), "body");
            JSONObject tokenBodyObj = new JSONObject(tokenBody);
            String tokenScopes = tokenBodyObj.getString("scope");
            if (!isCustomerCareOfficer(tokenScopes)) {
                // user is not a customer care officer
                ArrayList optUserIds = (ArrayList) consentAdminData.getQueryParams().get(userIdParamName);
                String optUserId = !optUserIds.isEmpty() ? getUserNameWithTenantDomain(optUserIds.get(0).toString())
                        : "";
                String tokenSubject = getUserNameWithTenantDomain(tokenBodyObj.getString("sub"));
                if (StringUtils.isEmpty(optUserId) || !isUserIdMatchesTokenSub(optUserId, tokenSubject)) {
                    // token subject and user id do not match, invalid request
                    final String errorMsg = "Invalid self care portal request received. " +
                            "UserId and token subject do not match.";
                    log.error(errorMsg);
                    throw new ConsentException(ResponseStatus.UNAUTHORIZED, errorMsg);
                }
            }
        } catch (ParseException e) {
            throw new ConsentException(ResponseStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Method to match customer care officer scopes.
     *
     * @param scopes scopes received from access token
     * @return if customer care officer scope found return true else false
     */
    protected boolean isCustomerCareOfficer(String scopes) {
        if (StringUtils.isNotEmpty(scopes)) {
            return scopes.contains(ConsentConstants.CUSTOMER_CARE_OFFICER_SCOPE);
        }
        return false;
    }

    /**
     * Method to get the userName with tenant domain.
     *
     * @param userName username
     * @return username with tenant domain
     */
    public static String getUserNameWithTenantDomain(String userName) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        if (userName.endsWith(tenantDomain)) {
            return userName;
        } else {
            return userName + "@" + tenantDomain;
        }
    }

    /**
     * Method to match user id and token subject.
     *
     * @param userId   received from query parameter
     * @param tokenSub received from access token body
     * @return if user id matches with token subject return true else false
     */
    protected boolean isUserIdMatchesTokenSub(String userId, String tokenSub) {
        if (StringUtils.isNotEmpty(tokenSub)) {
            return tokenSub.equals(userId);
        }
        return false;
    }
}
