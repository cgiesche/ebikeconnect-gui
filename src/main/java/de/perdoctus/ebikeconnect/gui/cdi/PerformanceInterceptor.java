package de.perdoctus.ebikeconnect.gui.cdi;

import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.concurrent.TimeUnit;

@LogPerformance
@Interceptor
public class PerformanceInterceptor {

    private final Logger logger;

    @Inject
    public PerformanceInterceptor(final Logger logger) {
        this.logger = logger;
    }

    @AroundInvoke
    public Object logTime(InvocationContext invocationContext) throws Exception {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        final Object proceed = invocationContext.proceed();
        logger.debug("Call to " + invocationContext.getMethod().getName() + " took " + stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
        return proceed;
    }

}
