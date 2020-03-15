package com.ak.fx.desktop;

import javax.annotation.Nonnull;

import com.ak.util.Strings;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

final class FxClassPathXmlApplicationContext extends GenericXmlApplicationContext {
  private static final String CONTEXT_XML = "context.xml";

  FxClassPathXmlApplicationContext(@Nonnull Class<?> clazz, @Nonnull String contextName) {
    super(new ClassPathResource(getContextPath(contextName), clazz));
    if (!contextName.isEmpty()) {
      setDisplayName(contextName);
    }
  }

  private static String getContextPath(@Nonnull String contextName) {
    if (contextName.isEmpty()) {
      contextName = CONTEXT_XML;
    }
    else {
      contextName = String.join(Strings.POINT, contextName, CONTEXT_XML);
    }
    return contextName;
  }
}
