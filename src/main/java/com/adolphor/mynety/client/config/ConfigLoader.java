package com.adolphor.mynety.client.config;

import com.adolphor.mynety.common.bean.BaseConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.adolphor.mynety.client.config.ProxyPacConfig.*;
import static com.adolphor.mynety.common.constants.Constants.*;

/**
 * load configuration
 * <p>
 * load config info
 *
 * @author Bob.Zhu
 * @Email adolphor@qq.com
 * @since v0.0.1
 */
@Slf4j
public class ConfigLoader {

  private final static String pacFileName = "pac.yaml";
  private final static String configFileName = "client-config.yaml";

  /**
   * <p>
   * load pac & client & server conf info
   *
   * @throws Exception
   */
  public static void loadConfig() throws Exception {

    loadPac(pacFileName);

    loadClientConf(configFileName);

  }

  private static void loadPac(String pacFile) throws Exception {
    try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream(pacFile)) {
      if (is == null) {
        return;
      }

      Map pac = new Yaml().load(is);
      if (pac.get(CONN_PROXY) != null) {
        PROXY_DOMAINS.addAll((List) pac.get(CONN_PROXY));
      }
      if (pac.get(CONN_DIRECT) != null) {
        DIRECT_DOMAINS.addAll((List) pac.get(CONN_DIRECT));
      }
      if (pac.get(CONN_DENY) != null) {
        DENY_DOMAINS.addAll((List) pac.get(CONN_DENY));
      }
    }
  }


  private static void loadClientConf(String configFile) throws Exception {

    Map config = BaseConfigLoader.loadConfig(configFile);

    Object aPublic = config.get("public");
    if (aPublic != null) {
      Config.IS_PUBLIC = Boolean.valueOf(aPublic.toString());
    }
    Object socksLocalPort = config.get("socksLocalPort");
    if (socksLocalPort != null) {
      Config.SOCKS_PROXY_PORT = Integer.parseInt(socksLocalPort.toString());
    }
    Object httpLocalPort = config.get("httpLocalPort");
    if (httpLocalPort != null) {
      Config.HTTP_PROXY_PORT = Integer.parseInt(httpLocalPort.toString());
    }
    Object http2socks5 = config.get("http2socks5");
    if (http2socks5 != null) {
      Config.HTTP_2_SOCKS5 = Boolean.valueOf(http2socks5.toString());
    }
    Object httpMitm = config.get("httpMitm");
    if (httpMitm != null) {
      Map httpMitmMap = (Map) httpMitm;
      Object isOpen = httpMitmMap.get("isOpen");
      if (isOpen != null) {
        Config.HTTP_MITM = Boolean.valueOf(isOpen.toString());
      }
      Object caPassword = httpMitmMap.get("caPassword");
      if (caPassword != null) {
        Config.CA_PASSWORD = caPassword.toString();
      }
      Object caKeyStoreFile = httpMitmMap.get("caKeyStoreFile");
      if (caKeyStoreFile != null) {
        Config.CA_KEYSTORE_FILE = caKeyStoreFile.toString();
      }
    }
    Object proxyStrategy = config.get("proxyStrategy");
    if (proxyStrategy != null) {
      Config.PROXY_STRATEGY = Integer.parseInt(proxyStrategy.toString());
    }
  }

}
