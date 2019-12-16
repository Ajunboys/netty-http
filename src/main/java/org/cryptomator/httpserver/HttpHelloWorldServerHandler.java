package org.cryptomator.httpserver;

 
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;

import static io.netty.handler.codec.http.HttpHeaderNames.*;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHelloWorldServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static byte[] CONTENT = { 'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd' };
    private static final String FAVICON_ICO = "/favicon.ico";
	private static final String TEXT_PLAIN = "text/plain";
    
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @SuppressWarnings("unlikely-arg-type")
	@Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;

            String method = String.valueOf(req.method());
            
            System.out.println("request: " + req.uri() + "," + method);
            boolean keepAlive = HttpUtil.isKeepAlive(req);
            String res = "(*-*):Thank you visit to the server.";


            if (req.uri().equals("/init?") || req.uri().startsWith("/init?")) {
               res = "1";
            } else if (req.uri().startsWith("/connect?")) {
         	   res = "1";
         	   if (method.equals("GET")) {
         		  QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
    			  Map<String, List<String>> parame = decoder.parameters();
    			  Set<String> keySet = parame.keySet();
    			  for (String key : keySet) {
    				  System.out.println("param name:"+key+"," + parame.get(key));
    			  }
         	   } else if (method.equals("POST")) {
         		  HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
      					new DefaultHttpDataFactory(false), req);
         		  InterfaceHttpData postData = decoder.getBodyHttpData("q"); // //
         		 String question = "";
     			if (postData.getHttpDataType() == HttpDataType.Attribute) {
     				Attribute attribute = (Attribute) postData;
     				try {
						question = attribute.getValue();
					} catch (IOException e) {
						
					}
     				System.out.println("q:" + question);

     			}
     			if (question != null && !question.equals("")) {

     				 

     			}
         	   }
         	  
            } else if (req.uri().equals("/exit")) {
            	res = "exit...";
			} else if (req.uri().equals("/favicon.ico")) {
            	return;
            } 
            
            try {
            	CONTENT = res.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("e:" + e.getMessage());
			}

            FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), OK,
                           Unpooled.wrappedBuffer(CONTENT));
            response.headers()
            .set(CONTENT_TYPE, TEXT_PLAIN)
            .setInt(CONTENT_LENGTH, response.content().readableBytes());

            ChannelFuture f = ctx.write(response);


            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault()) {
                   response.headers().set(CONNECTION, KEEP_ALIVE);
                }
            } else {
                // Tell the client we're going to close the connection.
                response.headers().set(CONNECTION, ChannelFutureListener.CLOSE);
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
