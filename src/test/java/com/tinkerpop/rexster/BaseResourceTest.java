package com.tinkerpop.rexster;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class BaseResourceTest {

    @Test
    public void testQueryParametersToJson() throws JSONException {
        Map<String, String> qp = new HashMap<String, String>();
        qp.put("a", "true");
        qp.put("b", "false");
        qp.put("c.a", "12.0");
        qp.put("c.b", "\"marko\"");
        qp.put("c.c", "peter");
        qp.put("c.d.a.b", "true");
        qp.put("d", "[marko,rodriguez,10]");
        
        BaseResource tt = new MockResource(qp);
        
        Assert.assertTrue(tt.getRequestObject().optBoolean("a"));
        Assert.assertFalse(tt.getRequestObject().optBoolean("b"));
        Assert.assertEquals(12.0, tt.getRequestObject().optJSONObject("c").optDouble("a"), 0);
        Assert.assertEquals("\"marko\"", tt.getRequestObject().optJSONObject("c").optString("b"));
        Assert.assertEquals("peter", tt.getRequestObject().optJSONObject("c").optString("c"));
        Assert.assertTrue(tt.getRequestObject().optJSONObject("c").optJSONObject("d").optJSONObject("a").optBoolean("b"));
        Assert.assertEquals("marko", tt.getRequestObject().optJSONArray("d").optString(0));
        Assert.assertEquals("rodriguez", tt.getRequestObject().optJSONArray("d").optString(1));
        // TODO: make this not a string but a number?
        Assert.assertEquals("10", tt.getRequestObject().optJSONArray("d").optString(2));
    }

    @Test
    public void getStartOffsetEmptyRequest() {
        BaseResource tt = new MockResource();
        Assert.assertEquals(new Long(0), tt.getStartOffset());
    }

    @Test
    public void getStartOffsetNoOffset() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"anyotherproperty\": { \"start\":\"ten\", \"end\":100 }}}");
        Assert.assertEquals(new Long(0), tt.getStartOffset());
    }

    @Test
    public void getStartOffsetInvalidOffset() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":\"ten\", \"end\":100 }}}");
        Assert.assertEquals(0l, (long) tt.getStartOffset());
    }
    
    @Test
    public void getStartOffsetWithNoStart() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"end\":100 }}}");
        Assert.assertEquals(0l, (long) tt.getStartOffset());
    }

    @Test
    public void getStartOffsetValid() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":10, \"end\":100 }}}");
        Assert.assertEquals(10l, (long) tt.getStartOffset());

        tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":-10, \"end\":10001 }}}");
        Assert.assertEquals(-10l, (long) tt.getStartOffset());
    }

    @Test
    public void getEndOffsetEmptyRequest() {
        BaseResource tt = new MockResource();
        Assert.assertEquals(new Long(Long.MAX_VALUE), tt.getEndOffset());
    }
    
    @Test
    public void getEndOffsetStartWithNoEnd() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":10 }}}");
        Assert.assertEquals(new Long(Long.MAX_VALUE), tt.getEndOffset());
    }

    @Test
    public void getEndOffsetNoOffset() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"anyotherproperty\": { \"start\":10, \"end\":100 }}}");
        Assert.assertEquals(new Long(Long.MAX_VALUE), tt.getEndOffset());
    }

    @Test
    public void getEndOffsetInvalidOffset() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":10, \"end\":\"onehundred\" }}}");
        Assert.assertEquals(0l, (long) tt.getEndOffset());
    }

    @Test
    public void getEndOffsetValid() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":10, \"end\":100 }}}");
        Assert.assertEquals(100l, (long) tt.getEndOffset());

        tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"offset\": { \"start\":-10, \"end\":10001 }}}");
        Assert.assertEquals(10001l, (long) tt.getEndOffset());
    }

    @Test
    public void getReturnKeysEmptyRequest() {
        BaseResource tt = new MockResource();
        Assert.assertEquals(new Long(Long.MAX_VALUE), tt.getEndOffset());
    }

    @Test
    public void getReturnKeysNoKeys() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"someproperty\": [ \"key\" ]}}");
        Assert.assertNull(tt.getReturnKeys());
    }

    @Test
    public void getReturnKeysValid() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"return_keys\": [ \"key1\" ]}}");
        Assert.assertNotNull(tt.getReturnKeys());
        Assert.assertEquals(1, tt.getReturnKeys().size());
        Assert.assertEquals("key1", tt.getReturnKeys().get(0));

        tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"return_keys\": [ \"key1\", \"key2\", \"key3\" ]}}");
        Assert.assertNotNull(tt.getReturnKeys());
        Assert.assertEquals(3, tt.getReturnKeys().size());
        Assert.assertEquals("key1", tt.getReturnKeys().get(0));
        Assert.assertEquals("key2", tt.getReturnKeys().get(1));
        Assert.assertEquals("key3", tt.getReturnKeys().get(2));
    }
    
    @Test
    public void getReturnKeysSingleKeyValid() {
        BaseResource tt = new MockResource();
        tt.buildRequestObject("{\"rexster\": { \"return_keys\": \"key1\"}}");
        Assert.assertNotNull(tt.getReturnKeys());
        Assert.assertEquals(1, tt.getReturnKeys().size());
        Assert.assertEquals("key1", tt.getReturnKeys().get(0));
    }

    @Test
    public void addHeadersAllPresent() {
        MockResource mock = new MockResource();
        ResponseBuilder builder = mock.addHeaders(Response.ok());

        Assert.assertNotNull(builder);

        Response response = builder.build();
        MultivaluedMap<String, Object> map = response.getMetadata();
        Assert.assertNotNull(map);

        // without this the web tool for rexster won't work
        Assert.assertTrue(map.containsKey(BaseResource.HEADER_ACCESS_CONTROL_ALLOW_ORIGIN));

    }

    protected class MockResource extends BaseResource {
    	public MockResource() {
    		super(new MockRexsterApplicationProvider());
    	}
    	
        public MockResource(Map map) {
            super(new MockRexsterApplicationProvider());
            this.httpServletRequest = new MockHttpServletRequest(map); 
        }

        public ResponseBuilder addHeaders(ResponseBuilder builder) {
            return super.addHeaders(builder);
        }
        
        public void setRequestObject(JSONObject queryParameters) {
        	super.setRequestObject(queryParameters);
        }
    }
    
    protected class MockHttpServletRequest implements HttpServletRequest {

    	private Map map = new HashMap();
    	
    	public MockHttpServletRequest(Map map) {
    		this.map = map;
    	}
    	
		@Override
		public Object getAttribute(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getContentLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getLocalAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getLocalName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getLocalPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getLocales() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getParameter(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map getParameterMap() {
			return map;
		}

		@Override
		public Enumeration getParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String[] getParameterValues(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public BufferedReader getReader() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRealPath(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteAddr() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteHost() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getRemotePort() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getScheme() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServerName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getServerPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isSecure() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeAttribute(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setAttribute(String arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCharacterEncoding(String arg0)
				throws UnsupportedEncodingException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getAuthType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContextPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getDateHeader(String arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getHeader(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration getHeaders(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getIntHeader(String arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPathInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getPathTranslated() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getQueryString() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRemoteUser() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRequestURI() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServletPath() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpSession getSession() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HttpSession getSession(boolean arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Principal getUserPrincipal() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isUserInRole(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}
    	
    }

    protected class MockRexsterApplicationProvider implements RexsterApplicationProvider {

        private final long startTime = System.currentTimeMillis();

        @Override
        public RexsterApplication getRexsterApplication() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public RexsterApplicationGraph getApplicationGraph(String graphName) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ResultObjectCache getResultObjectCache() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Set<String> getGraphsNames() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getStartTime() {
            return this.startTime;
        }

    }
}

