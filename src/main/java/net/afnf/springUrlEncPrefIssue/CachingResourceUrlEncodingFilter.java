/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.afnf.springUrlEncPrefIssue;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;
import org.springframework.web.servlet.resource.ResourceUrlProvider;
import org.springframework.web.servlet.resource.ResourceUrlProviderExposingInterceptor;

/**
 * CachingResourceUrlEncodingFilter
 * 
 * @author afnf
 */
public class CachingResourceUrlEncodingFilter extends ResourceUrlEncodingFilter {

    private String prefix;

    public CachingResourceUrlEncodingFilter() {
    }

    public CachingResourceUrlEncodingFilter(String prefix) {
        this.prefix = prefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, new CachingResourceUrlEncodingResponseWrapper(request, response, prefix));
    }

    private static class CachingResourceUrlEncodingResponseWrapper extends HttpServletResponseWrapper {
        private HttpServletRequest request;
        private Integer indexLookupPath;
        private String prefix;
        private String contextPath;
        private static Map<String, String> resolveCache = new ConcurrentHashMap<>();

        public CachingResourceUrlEncodingResponseWrapper(HttpServletRequest request, HttpServletResponse wrapped, String prefix) {
            super(wrapped);
            this.request = request;
            this.prefix = prefix;
            this.contextPath = request.getContextPath();
        }

        @Override
        public String encodeURL(String url) {

            String value = resolveCache.get(url);
            if (value != null) {
                return value;
            }
            else {
                if (prefix == null || url.startsWith(contextPath + prefix)) {
                    ResourceUrlProvider resourceUrlProvider = getResourceUrlProvider();
                    if (resourceUrlProvider != null) {
                        initIndexLookupPath(resourceUrlProvider);
                        if (url.length() >= this.indexLookupPath) {
                            String prefix = url.substring(0, this.indexLookupPath);
                            String lookupPath = url.substring(this.indexLookupPath);
                            lookupPath = resourceUrlProvider.getForLookupPath(lookupPath);
                            if (lookupPath != null) {
                                value = super.encodeURL(prefix + lookupPath);
                                resolveCache.put(url, value);
                                return value;
                            }
                        }
                    }
                }
                value = super.encodeURL(url);
                return value;
            }
        }

        private ResourceUrlProvider getResourceUrlProvider() {
            String name = ResourceUrlProviderExposingInterceptor.RESOURCE_URL_PROVIDER_ATTR;
            return (ResourceUrlProvider) this.request.getAttribute(name);
        }

        private void initIndexLookupPath(ResourceUrlProvider urlProvider) {
            if (this.indexLookupPath == null) {
                String requestUri = urlProvider.getPathHelper().getRequestUri(this.request);
                String lookupPath = urlProvider.getPathHelper().getLookupPathForRequest(this.request);
                this.indexLookupPath = requestUri.lastIndexOf(lookupPath);
            }
        }
    }
}
