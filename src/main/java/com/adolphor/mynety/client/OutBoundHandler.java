package com.adolphor.mynety.client;

import com.adolphor.mynety.common.wrapper.AbstractOutBoundHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import static com.adolphor.mynety.common.constants.Constants.ATTR_IN_RELAY_CHANNEL;

/**
 * @author Bob.Zhu
 * @Email adolphor@qq.com
 * @since v0.0.1
 */
@Slf4j
@ChannelHandler.Sharable
public final class OutBoundHandler extends AbstractOutBoundHandler<ByteBuf> {

  public static final OutBoundHandler INSTANCE = new OutBoundHandler();

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
    Channel inRelayChannel = ctx.channel().attr(ATTR_IN_RELAY_CHANNEL).get();
    inRelayChannel.writeAndFlush(msg);
  }

}
