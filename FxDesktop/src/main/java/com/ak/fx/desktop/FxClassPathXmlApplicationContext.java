package com.ak.fx.desktop;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.util.Strings;
import org.springframework.context.support.ClassPathXmlApplicationContext;

final class FxClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
  private static final String CONTEXT_XML = "context.xml";
  @Nonnull
  private final String contextName;

  FxClassPathXmlApplicationContext(@Nullable String contextName) {
    super(getContextPath(contextName));
    this.contextName = getContextName(contextName);
  }

  @Override
  public String getApplicationName() {
    return contextName;
  }

  private static String getContextPath(@Nullable String contextName) {
    contextName = getContextName(contextName);
    URL resource = FxClassPathXmlApplicationContext.class.getResource(CONTEXT_XML);
    if (!contextName.isEmpty()) {
      resource = FxClassPathXmlApplicationContext.class.getResource(String.format("%s/%s", contextName, CONTEXT_XML));
    }
    return Paths.get(resource.toExternalForm()).toString();
  }

  private static String getContextName(@Nullable String contextName) {
    return Optional.ofNullable(contextName).orElse(Strings.EMPTY).replaceAll("\\.", "/");
  }
}
