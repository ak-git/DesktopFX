package com.ak.fx.desktop;

import javax.annotation.Nonnull;

import com.ak.util.PropertiesSupport;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import static com.ak.util.Strings.pointConcat;

final class FxClassPathXmlApplicationContext extends GenericXmlApplicationContext {
  private static final String CONTEXT_XML = "context.xml";

  FxClassPathXmlApplicationContext(@Nonnull Class<?> clazz) {
    super(new ClassPathResource(getContextPath(), clazz));
  }

  private static String getContextPath() {
    String contextName = PropertiesSupport.CONTEXT.value();
    if (contextName.isEmpty()) {
      contextName = CONTEXT_XML;
    }
    else {
      contextName = pointConcat(contextName, CONTEXT_XML);
    }
    return contextName;
  }
}
