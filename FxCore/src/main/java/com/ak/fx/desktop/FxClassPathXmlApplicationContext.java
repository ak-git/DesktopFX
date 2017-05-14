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
    super(getContextPath(Optional.ofNullable(contextName).orElse(Strings.EMPTY)));
    this.contextName = Optional.ofNullable(contextName).orElse(Strings.EMPTY);
  }

  @Override
  public String getApplicationName() {
    return contextName;
  }

  private static String getContextPath(@Nonnull String contextName) {
    Path path = Paths.get(FxClassPathXmlApplicationContext.class.getPackage().getName().replaceAll("\\.", "/"));
    if (contextName.isEmpty()) {
      path = path.resolve(CONTEXT_XML);
    }
    else {
      path = path.resolve(contextName).resolve(String.format("%s-%s", contextName, CONTEXT_XML));
    }
    return path.toString();
  }
}
