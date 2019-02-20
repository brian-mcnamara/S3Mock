package com.adobe.testing.s3mock;

import com.adobe.testing.s3mock.domain.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * Filter to enable the new(er) s3 virtual host (subdomain) prefix to specify the s3 bucket.
 * This maps those calls into the older URI format.
 */
@Component
public class VirtualHostEndpointFilter implements Filter {

    @Autowired
    private FileStore fileStore;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String serverName = request.getServerName();
        if (serverName.contains(".")) {
            String bucketName = serverName.split("\\.")[0];
            if (fileStore.getBucket(bucketName) != null) {
                request = new VirtualHostEndpointConverter(request, bucketName);
            }
        }
        filterChain.doFilter(request, servletResponse);
    }

    private static final class VirtualHostEndpointConverter extends HttpServletRequestWrapper {
        private String bucket;

        public VirtualHostEndpointConverter(HttpServletRequest request, String bucket) {
            super(request);
            this.bucket = bucket;
        }

        @Override
        public String getServletPath() {
            return "/" +  bucket + super.getServletPath();
        }
    }
}
