package gfxorwfemscallbackproxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@WebFilter(urlPatterns = "/integration/callback/*")
public class WSDLConfig { //implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(WSDLConfig.class);
}

/*
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if ("GET".equals(request.getMethod()) && "wsdl".equalsIgnoreCase(request.getQueryString())) {
            request.getSession().getServletContext().getRequestDispatcher(request.getRequestURI() + ".wsdl").forward(request, response);
        }

        LOGGER.info("filter:" + ((HttpServletRequest) servletRequest).getRequestURL());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }
}*/
