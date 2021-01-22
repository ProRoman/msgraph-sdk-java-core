package com.microsoft.graph.authentication;

import com.azure.core.credential.TokenCredential;
import com.microsoft.graph.exceptions.AuthenticationException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.mocks.MockTokenCredential;
import com.microsoft.graph.options.HeaderOption;

import okhttp3.Request;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TokenCredentialAuthProviderTest {

    public final String testToken = "CredentialTestToken";
    private static final HashSet<String> validGraphHostNames = new HashSet<>(Arrays.asList("graph.microsoft.com", "graph.microsoft.us", "dod-graph.microsoft.us", "graph.microsoft.de", "microsoftgraph.chinacloudapi.cn"));

    @Test
    public void TokenCredentialAuthProviderTestICoreAuthentication() throws AuthenticationException {

        for(final String hostName : validGraphHostNames) {
            // Arrange
            final Request request = new Request.Builder().url("https://" + hostName).build();
            final TokenCredential mockCredential = MockTokenCredential.getMockTokenCredential();
            final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(mockCredential);

            // Act
            Assert.assertNull(request.header(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
            final Request authenticatedRequest = authProvider.authenticateRequest(request);

            // Assert
            Assert.assertEquals(request.url(), authenticatedRequest.url());
            Assert.assertNotNull(authenticatedRequest.header(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
            assertEquals(TokenCredentialAuthProvider.BEARER + testToken, authenticatedRequest.header(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
        }
    }

    @Test
    public void TokenCredentialAuthProviderTestIAuthentication() throws AuthenticationException, MalformedURLException {

        for(final String hostName : validGraphHostNames) {
            // Arrange
            final TokenCredential mockCredential = MockTokenCredential.getMockTokenCredential();
            final IHttpRequest request = mock(IHttpRequest.class);
            final List<HeaderOption> headersOptions = new ArrayList<>();
            when(request.getRequestUrl()).thenReturn(new URL("https://" + hostName));
            doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    headersOptions.add(new HeaderOption((String)invocation.getArguments()[0], (String)invocation.getArguments()[1]));
                    return null;
                }

            }).when(request).addHeader(any(String.class), any(String.class));
            when(request.getHeaders()).thenReturn(headersOptions);
            final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(mockCredential);

            //Act
            Assert.assertTrue(request.getHeaders().isEmpty());
            authProvider.authenticateRequest(request);
            Assert.assertFalse(request.getHeaders().isEmpty());

            //Assert
            Assert.assertTrue(request.getHeaders().get(0).getName().equals(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
            Assert.assertTrue(request.getHeaders().get(0).getValue().equals(TokenCredentialAuthProvider.BEARER + this.testToken));
        }
    }
    @Test
    public void TokenCredentialAuthProviderDoesNotAddTokenOnInvalidDomainsTestICoreAuthentication() throws AuthenticationException {

        //Arrange
        final Request request = new Request.Builder().url("https://localhost").build();
        final TokenCredential mockCredential = MockTokenCredential.getMockTokenCredential();
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(mockCredential);

        //Act
        Assert.assertNull(request.header(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
        final Request authenticatedRequest = authProvider.authenticateRequest(request);

        //Assert
        Assert.assertEquals(request.url(), authenticatedRequest.url());
        Assert.assertNull(authenticatedRequest.header(TokenCredentialAuthProvider.AUTHORIZATION_HEADER));
    }

    @Test
    public void TokenCredentialAuthProviderDoesNotAddTokenOnInvalidDomainsTestIAuthentication() throws AuthenticationException,
            MalformedURLException {

        //Arrange
        final TokenCredential mockCredential = MockTokenCredential.getMockTokenCredential();
        final IHttpRequest request = mock(IHttpRequest.class);
        when(request.getRequestUrl()).thenReturn(new URL("https://localhost"));
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(mockCredential);

        //Act
        Assert.assertTrue(request.getHeaders().isEmpty());
        authProvider.authenticateRequest(request);

        //Assert
        Assert.assertTrue(request.getHeaders().isEmpty());
    }
    @Test
    public void TokenCredentialAuthProviderDoesNotAddTokenOnInvalidProtocols() throws AuthenticationException,
            MalformedURLException {

        //Arrange
        final TokenCredential mockCredential = MockTokenCredential.getMockTokenCredential();
        final IHttpRequest request = mock(IHttpRequest.class);
        when(request.getRequestUrl()).thenReturn(new URL("http://graph.microsoft.com"));
        final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(mockCredential);

        //Act
        Assert.assertTrue(request.getHeaders().isEmpty());
        authProvider.authenticateRequest(request);

        //Assert
        Assert.assertTrue(request.getHeaders().isEmpty());
    }
}
