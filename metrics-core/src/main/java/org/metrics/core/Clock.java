package org.metrics.core;

public abstract class Clock {
	/**
     * Returns the current time tick.
     *
     * @return time tick in nanoseconds
     */
    public abstract long getTick();

    /**
     * Returns the current time in milliseconds.
     *
     * @return time in milliseconds
     */
    public long getTime() {
        return System.currentTimeMillis();
    }

    /**
     * The default clock to use.
     *
     * @return the default {@link Clock} instance
     * @see Clock.SystemClock
     */
    public static Clock defaultClock() {
        return SystemClockHolder.DEFAULT;
    }

    /**
     * A clock implementation which returns the current time in epoch nanoseconds.
     */
    public static class SystemClock extends Clock {
        @Override
        public long getTick() {
            return System.nanoTime();
        }
    }

    private static class SystemClockHolder {
        private static final Clock DEFAULT = new SystemClock();
    }
}
