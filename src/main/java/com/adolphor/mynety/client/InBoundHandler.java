package com.adolphor.mynety.client;

import com.adolphor.mynety.client.utils.PacFilter;
import com.adolphor.mynety.common.constants.Constants;
import com.adolphor.mynety.common.wrapper.AbstractInBoundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.handler.codec.socksx.v5.Socks5Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

import static com.adolphor.mynety.common.constants.Constants.*;

/**
 * 连接处理器：建立本地和远程服务器（代理服务器或者是目的服务器，根据代理规则来确定）的连接，
 *
 * @author Bob.Zhu
 * @Email adolphor@qq.com
 * @since v0.0.1
 */
@Slf4j
@ChannelHandler.Sharable
public final class InBoundHandler extends AbstractInBoundHandler<ByteBuf> {

  public static final InBoundHandler INSTANCE = new InBoundHandler();

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);

    Socks5CommandRequest socksRequest = ctx.channel().attr(ATTR_SOCKS5_REQUEST).get();
    String dstAddr = socksRequest.dstAddr();
    Integer dstPort = socksRequest.dstPort();
    boolean isDeny = PacFilter.isDeny(dstAddr);
    if (isDeny) {
      logger.info("[ {}{} ] deny request by pac rules => {}:{}", ctx.channel().id(), Constants.LOG_MSG, dstAddr, dstPort);
      channelClose(ctx);
      return;
    }
    Boolean isProxy = PacFilter.isProxy(dstAddr);
    ctx.channel().attr(ATTR_IS_PROXY).set(isProxy);

    Bootstrap outBoundBootStrap = new Bootstrap();
    outBoundBootStrap.group(ctx.channel().eventLoop())
        .channel(Constants.channelClass)
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECT_TIMEOUT)
        .handler(OutBoundInitializer.INSTANCE);

    outBoundBootStrap.connect(dstAddr, dstPort).addListener((ChannelFutureListener) future -> {
      if (future.isSuccess()) {
        Channel outRelayChannel = future.channel();
        ctx.channel().attr(ATTR_OUT_RELAY_CHANNEL_REF).get().set(outRelayChannel);
        outRelayChannel.attr(ATTR_IN_RELAY_CHANNEL).set(ctx.channel());
        Socks5Message socks5cmdResponse = new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, socksRequest.dstAddrType(), dstAddr, socksRequest.dstPort());
        ctx.channel().writeAndFlush(socks5cmdResponse);
      } else {
        throw new Exception(future.cause());
      }
    });
  }

  /**
   * 客户端 与 SocksClient 之间建立的 Channel 称为 inRelayChannel；
   * SocksClient 与 目标地址 建立的，称为 OutboundChannel。
   *
   * @param ctx
   * @param msg
   * @throws Exception
   */
  @Override
  public void channelRead0(final ChannelHandlerContext ctx, ByteBuf msg) {
    if (msg.readableBytes() <= 0) {
      return;
    }

    AtomicReference<Channel> outRelayChannelRef = ctx.channel().attr(ATTR_OUT_RELAY_CHANNEL_REF).get();
    outRelayChannelRef.get().writeAndFlush(msg);
  }

}
