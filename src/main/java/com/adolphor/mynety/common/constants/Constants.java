package com.adolphor.mynety.common.constants;

import com.adolphor.mynety.common.bean.Address;
import com.adolphor.mynety.common.bean.BaseConfig;
import com.adolphor.mynety.common.wrapper.AbstractOutBoundHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * @author Bob.Zhu
 * @Email adolphor@qq.com
 * @since v0.0.1
 */
@Slf4j
public class Constants {

  /**
   * some default value of url
   */
  public static final int PORT_80 = 80;
  public static final int PORT_443 = 443;
  public static final String SCHEME_HTTPS = "https";
  public static final String SCHEME_HTTP = "http";
  public static final String LOOPBACK_ADDRESS = "127.0.0.1";
  public static final String ALL_LOCAL_ADDRESS = "0.0.0.0";
  public static final String COLON = ":";
  public static final int MAX_CONTENT_LENGTH = 6553600;

  public static final LogLevel LOG_LEVEL = LogLevel.TRACE;

  /**
   * proxy strategy
   */
  public static final String CONN_PROXY = "proxy";
  public static final String CONN_DIRECT = "direct";
  public static final String CONN_DENY = "deny";

  /**
   * max wait time
   */
  public static final int CONNECT_TIMEOUT = 3 * 1000;

  /**
   * logger flag
   */
  public static final String LOG_MSG = " <==> ";
  public static final String LOG_MSG_OUT = " >>> ";
  public static final String LOG_MSG_IN = " <<< ";
  /**
   * Regular check rules
   */
  public static final Pattern IPV4_PATTERN = Pattern.compile("(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])");
  public static final Pattern IPV6_PATTERN = Pattern.compile("([0-9a-f]{1,4}:){3}([0-9a-f]){1,4}");

  /**
   * httpTunnel response
   */
  public static final HttpResponseStatus CONNECTION_ESTABLISHED = new HttpResponseStatus(HttpResponseStatus.OK.code(), "Connection established");

  /**
   * encryption/decryption
   */
  public static final String ENCRYPT_NONE = "none";
  public static final String CRYPT_TYPE_AES = "AES";
  public static final String CRYPT_TYPE_AES_OFB = "ofb";
  public static final String CRYPT_TYPE_BF = "BF";
  public static final String CRYPT_TYPE_CL = "CL";
  public static final String CRYPT_TYPE_SD = "SD";

  /**
   * 保留字节，填充空值
   */
  public static final int RESERVED_BYTE = 0x00;

  /**
   * 是否是http tunnel代理（inRelayChannel：httpProxy中使用）
   */
  public static final AttributeKey<Boolean> ATTR_IS_HTTP_TUNNEL = AttributeKey.valueOf("is.http.tunnel");
  /**
   * inRelayChannel (outRelayChannel)
   */
  public static final AttributeKey<Channel> ATTR_IN_RELAY_CHANNEL = AttributeKey.valueOf("in.relay.channel");
  /**
   * 是否使用代理（inRelayChannel，通道激活的时候就会赋值）
   */
  public static final AttributeKey<Boolean> ATTR_IS_PROXY = AttributeKey.valueOf("is.proxy");
  /**
   * outRelayChannel（inRelayChannel）
   */
  public static final AttributeKey<AtomicReference<Channel>> ATTR_OUT_RELAY_CHANNEL_REF = AttributeKey.valueOf("out.relay.channel.ref");
  /**
   * 详情参考 {@link AbstractOutBoundHandler#channelActive} 的备注
   */
  public static final AttributeKey<AtomicReference> ATTR_REQUEST_TEMP_MSG = AttributeKey.valueOf("request.temp.msg");
  /**
   * ss-local 建立连接的时候使用
   */
  public static final AttributeKey<Socks5CommandRequest> ATTR_SOCKS5_REQUEST = AttributeKey.valueOf("socks5.request");
  /**
   * 请求地址（inRelayChannel：client-HttpProxyHandler & server-AddressHandler 中使用）
   */
  public static final AttributeKey<Address> ATTR_REQUEST_ADDRESS = AttributeKey.valueOf("request.address");
  /**
   * 连接成功的时间戳（inRelayChannel，outRelayChannel）
   */
  public static final AttributeKey<Long> ATTR_CONNECTED_TIMESTAMP = AttributeKey.valueOf("is.connected");
  /**
   *
   */
  public static Class channelClass;
  public static Class serverChannelClass;
  public static Constructor bossGroupType;
  public static Constructor workerGroupType;

  static {
    try {
      if (BaseConfig.nativeNio && SystemUtils.IS_OS_MAC) {
        logger.debug("macOS or BSD system ...");
        Constants.bossGroupType = KQueueEventLoopGroup.class.getDeclaredConstructor();
        Constants.workerGroupType = KQueueEventLoopGroup.class.getDeclaredConstructor();
        Constants.serverChannelClass = KQueueServerSocketChannel.class;
        Constants.channelClass = KQueueSocketChannel.class;
      } else if (BaseConfig.nativeNio && SystemUtils.IS_OS_LINUX) {
        logger.debug("linux system...");
        Constants.bossGroupType = EpollEventLoopGroup.class.getDeclaredConstructor();
        Constants.workerGroupType = EpollEventLoopGroup.class.getDeclaredConstructor();
        Constants.serverChannelClass = EpollServerSocketChannel.class;
        Constants.channelClass = EpollSocketChannel.class;
      } else {
        logger.debug("others system...");
        Constants.bossGroupType = NioEventLoopGroup.class.getDeclaredConstructor();
        Constants.workerGroupType = NioEventLoopGroup.class.getDeclaredConstructor();
        Constants.serverChannelClass = NioServerSocketChannel.class;
        Constants.channelClass = NioSocketChannel.class;
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

}