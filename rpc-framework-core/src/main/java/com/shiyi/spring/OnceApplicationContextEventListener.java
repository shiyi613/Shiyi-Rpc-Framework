package com.shiyi.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * @Author:shiyi
 * @create: 2023-05-23  12:19
 */
@Component
public abstract class OnceApplicationContextEventListener implements ApplicationListener, ApplicationContextAware {

    protected final Log log = LogFactory.getLog(getClass());

    protected static ApplicationContext applicationContext;

    public OnceApplicationContextEventListener(){

    }

    public OnceApplicationContextEventListener(ApplicationContext context){
        setApplicationContext(context);
    }

    public final void onApplicationEvent(ApplicationEvent event){
        if (isOriginalEventSource(event) && event instanceof ApplicationContextEvent) {
            onApplicationContextEvent((ApplicationContextEvent) event);
        }
    }

    /**
     * The subclass overrides this method to handle {@link ApplicationContextEvent}
     * @param event {@link ApplicationContextEvent}
     */
    protected abstract void onApplicationContextEvent(ApplicationContextEvent event);

    /**
     * Is original {@link ApplicationContext} as the event source
     * @param event {@link ApplicationEvent}
     * @return if original, return <code>true</code>, or <code>false</code>
     */
    private boolean isOriginalEventSource(ApplicationEvent event) {

        boolean originalEventSource = nullSafeEquals(getApplicationContext(), event.getSource());

        if (!originalEventSource) {
            if (log.isDebugEnabled()) {
                log.debug("The source of event[" + event.getSource() + "] is not original!");
            }
        }

        return originalEventSource;
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must be not null, it has to invoke " +
                    "setApplicationContext(ApplicationContext) method first if "
                    + ClassUtils.getShortName(getClass()) + " instance is not a Spring Bean");
        }
        return applicationContext;
    }

}
