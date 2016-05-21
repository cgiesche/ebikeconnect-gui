package de.perdoctus.ebikeconnect.gui.cdi;

/*
 * #%L
 * ebikeconnect-gui
 * %%
 * Copyright (C) 2016 Christoph Giesche
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


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
