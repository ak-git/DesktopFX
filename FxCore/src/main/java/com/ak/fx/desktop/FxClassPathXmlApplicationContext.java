package com.ak.fx.desktop;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.ak.util.Strings;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class FxClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {
  private static final String CONTEXT_XML = "context.xml";
  @Nonnull
  private final String contextName;

  public FxClassPathXmlApplicationContext(@Nullable String contextName) {
    super(getContextPath(contextName));
    this.contextName = getContextName(contextName);
  }

  @Override
  public String getApplicationName() {
    return contextName;
  }

  private static String getContextPath(@Nullable String contextName) {
    contextName = getContextName(contextName);
    Path path = Paths.get(FxClassPathXmlApplicationContext.class.getPackage().getName().replaceAll("\\.", "/"));
    if (contextName.isEmpty()) {
      path = path.resolve(CONTEXT_XML);
    }
    else {
      path = path.resolve(contextName).resolve(CONTEXT_XML);
    }
    return path.toString();
  }

  private static String getContextName(@Nullable String contextName) {
    return Optional.ofNullable(contextName).orElse(Strings.EMPTY).replaceAll("\\.", "/");
  }
}
